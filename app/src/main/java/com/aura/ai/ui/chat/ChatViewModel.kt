package com.aura.ai.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.voice.WakeWordService
import com.aura.ai.voice.WakeWordEvents
import com.aura.ai.agent.AgentEvent
import com.aura.ai.agent.AgentOrchestrator
import com.aura.ai.agent.AgentPhase
import com.aura.ai.data.settings.SettingsRepository
import com.aura.voice.AuraVoiceSubsystem
import com.aura.voice.ConversationState
import com.aura.voice.ConversationStateObserver
import com.aura.voice.SpeechInputConfig
import com.aura.voice.SpeechRecognitionListener
import com.aura.voice.SpeechRecognitionResult
import com.aura.voice.SpeechSynthesisListener
import com.aura.voice.VoiceError
import com.aura.voice.VoiceErrorCode
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: AgentOrchestrator,
    private val settings: SettingsRepository,
    private val voice: AuraVoiceSubsystem,
    private val wakeWordEvents: WakeWordEvents,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var runJob: Job? = null
    private var listenJob: Job? = null
    private val voiceStateSubscription = voice.observeState(object : ConversationStateObserver {
        override fun onStateChanged(snapshot: com.aura.voice.ConversationStateSnapshot) {
            _state.update {
                it.copy(
                    speaking = snapshot.state == ConversationState.SPEAKING,
                    listening = it.handsFreeSession && snapshot.state == ConversationState.LISTENING,
                )
            }
        }
    })

    init {
        viewModelScope.launch {
            settings.settings.collect { s ->
                _state.update {
                    it.copy(
                        modelReady = !s.selectedModel.isNullOrBlank(),
                        modelName = s.selectedModel,
                        banner = if (s.selectedModel.isNullOrBlank())
                            "No model selected. Open Settings to connect Ollama and pick a model." else null,
                    )
                }
            }
        }
        viewModelScope.launch {
            wakeWordEvents.events.collect { activateFromWakeWord() }
        }
    }

    fun onInputChange(value: String) = _state.update { it.copy(input = value) }

    fun send(text: String = _state.value.input) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _state.value.busy) return
        if (!_state.value.modelReady) return

        val userTurn = ChatTurn(id = System.nanoTime(), isUser = true, text = prompt)
        val assistantId = System.nanoTime() + 1
        val assistantTurn = ChatTurn(id = assistantId, isUser = false, text = "", streaming = true)

        _state.update {
            it.copy(
                turns = it.turns + userTurn + assistantTurn,
                input = "",
                busy = true,
            )
        }

        runJob?.cancel()
        runJob = viewModelScope.launch {
            voice.markProcessing()
            orchestrator.run(prompt).collect { event ->
                applyEvent(assistantId, event)
            }
            val finalText = _state.value.turns.firstOrNull { it.id == assistantId }?.text.orEmpty()
            _state.update { st ->
                st.copy(
                    busy = false,
                    turns = st.turns.map { if (it.id == assistantId) it.copy(streaming = false) else it },
                )
            }
            val voiceOn = settings.settings.first().voiceRepliesEnabled
            if (voiceOn && finalText.isNotBlank()) {
                val result = voice.speak(finalText, object : SpeechSynthesisListener {
                    override fun onStarted() = Unit
                    override fun onCompleted() = resumeHandsFreeAfterReply()
                    override fun onStopped() = Unit
                    override fun onError(error: VoiceError) {
                        _state.update { it.copy(banner = error.message) }
                        resumeHandsFreeAfterReply()
                    }
                })
                if (!result.accepted) {
                    _state.update { it.copy(banner = result.error?.message ?: "Voice output failed.") }
                    resumeHandsFreeAfterReply()
                }
            } else {
                resumeHandsFreeAfterReply()
            }
        }
    }

    private fun applyEvent(assistantId: Long, event: AgentEvent) {
        _state.update { st ->
            val turns = st.turns.toMutableList()
            val idx = turns.indexOfFirst { it.id == assistantId }
            if (idx < 0) return@update st
            var turn = turns[idx]

            fun addTrace(phase: AgentPhase, label: String, status: TraceStatus) {
                // Mark any previously-active line of the same phase as resolved.
                val trace = turn.trace.toMutableList()
                turn = turn.copy(trace = trace + TraceLine(phase, label, status))
            }

            when (event) {
                is AgentEvent.Phase -> addTrace(event.phase, event.detail, TraceStatus.ACTIVE)
                is AgentEvent.Understood -> addTrace(
                    AgentPhase.UNDERSTAND,
                    "Goal: ${event.understanding.goal}",
                    TraceStatus.OK,
                )
                is AgentEvent.Planned -> addTrace(
                    AgentPhase.PLAN,
                    "${event.plan.steps.size} step plan" + if (event.reusedSkill) " (reused a learned skill)" else "",
                    TraceStatus.OK,
                )
                is AgentEvent.StepStarted -> addTrace(
                    AgentPhase.EXECUTE,
                    "→ ${event.step.description}",
                    TraceStatus.ACTIVE,
                )
                is AgentEvent.StepObserved -> addTrace(
                    AgentPhase.EXECUTE,
                    if (event.observation.success) "✓ step ${event.observation.stepIndex}" else "✗ ${event.observation.error}",
                    if (event.observation.success) TraceStatus.OK else TraceStatus.FAIL,
                )
                is AgentEvent.Token -> turn = turn.copy(text = turn.text + event.text)
                is AgentEvent.Verified -> addTrace(
                    AgentPhase.VERIFY,
                    if (event.verdict.verified) "Verified against all criteria"
                    else "Not verified: ${event.verdict.unmetCriteria.joinToString("; ")}",
                    if (event.verdict.verified) TraceStatus.OK else TraceStatus.FAIL,
                )
                is AgentEvent.Replan -> addTrace(AgentPhase.PLAN, "Re-planning (attempt ${event.attempt + 1})", TraceStatus.INFO)
                is AgentEvent.Learned -> addTrace(AgentPhase.LEARN, event.note, TraceStatus.INFO)
                is AgentEvent.Answer -> if (turn.text.isBlank()) turn = turn.copy(text = event.text)
                is AgentEvent.Failed -> turn = turn.copy(
                    text = if (turn.text.isBlank()) event.message else turn.text,
                    error = true,
                )
            }
            turns[idx] = turn
            st.copy(turns = turns)
        }
    }

    // --- Voice input --------------------------------------------------------

    fun toggleListening() {
        val current = _state.value
        if (!current.handsFreeSession) {
            startHandsFreeSession()
        } else if (current.speaking) {
            voice.interrupt()
            startListeningTurn()
        } else {
            stopHandsFreeSession()
        }
    }

    private fun activateFromWakeWord() {
        startHandsFreeSession(announce = true)
    }

    private fun startHandsFreeSession(announce: Boolean = false) {
        WakeWordService.stop(context)
        _state.update { it.copy(handsFreeSession = true, banner = null) }
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            delay(350)
            if (announce && settings.settings.first().voiceRepliesEnabled) {
                val result = voice.speak("I'm listening.", object : SpeechSynthesisListener {
                    override fun onStarted() = Unit
                    override fun onCompleted() = startListeningTurn()
                    override fun onStopped() = Unit
                    override fun onError(error: VoiceError) {
                        _state.update { it.copy(banner = error.message) }
                        startListeningTurn()
                    }
                })
                if (!result.accepted) {
                    _state.update { it.copy(banner = result.error?.message ?: "Voice output failed.") }
                    startListeningTurn()
                }
            } else {
                startListeningTurn()
            }
        }
    }

    private fun startListeningTurn() {
        if (!_state.value.handsFreeSession) return
        listenJob?.cancel()
        _state.update { it.copy(listening = true, partialSpeech = "") }
        val startResult = voice.startListening(
            config = SpeechInputConfig(languageTag = "en-US", partialResults = true, preferOffline = false),
            listener = object : SpeechRecognitionListener {
                override fun onResult(result: SpeechRecognitionResult) {
                    val error = result.error
                    if (error != null) {
                        _state.update {
                            it.copy(listening = false, partialSpeech = "", banner = error.message)
                        }
                        if (error.code == VoiceErrorCode.NO_MATCH) resumeHandsFreeAfterReply()
                    } else if (result.isFinal) {
                        _state.update { it.copy(listening = false, partialSpeech = "") }
                        if (result.transcript.isNotBlank()) send(result.transcript)
                        else resumeHandsFreeAfterReply()
                    } else {
                        _state.update { it.copy(partialSpeech = result.transcript) }
                    }
                }
            },
        )
        if (!startResult.accepted) {
            _state.update {
                it.copy(
                    listening = false,
                    banner = startResult.error?.message ?: "Could not start speech recognition.",
                )
            }
        }
    }

    private fun stopHandsFreeSession() {
        listenJob?.cancel()
        voice.interrupt()
        _state.update {
            it.copy(
                handsFreeSession = false,
                listening = false,
                speaking = false,
                partialSpeech = "",
            )
        }
    }

    private fun resumeHandsFreeAfterReply() {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            delay(350)
            startListeningTurn()
        }
    }

    fun stopSpeaking() {
        voice.stopSpeaking()
        if (_state.value.handsFreeSession) startListeningTurn()
    }

    fun dismissBanner() = _state.update { it.copy(banner = null) }

    override fun onCleared() {
        listenJob?.cancel()
        voiceStateSubscription.close()
        super.onCleared()
    }
}
