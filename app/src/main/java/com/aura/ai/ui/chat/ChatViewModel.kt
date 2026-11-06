package com.aura.ai.ui.chat

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.aura.ai.voice.WakeWordService
import com.aura.ai.voice.WakeWordEvents
import com.aura.ai.voice.wake.WakeWordResumePolicy
import com.aura.ai.agent.AgentEvent
import com.aura.ai.agent.AgentOrchestrator
import com.aura.ai.agent.AgentPhase
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.repository.ConversationRepository
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: AgentOrchestrator,
    private val settings: SettingsRepository,
    private val conversationRepository: ConversationRepository,
    private val voice: AuraVoiceSubsystem,
    private val wakeWordEvents: WakeWordEvents,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var runJob: Job? = null
    private var listenJob: Job? = null
    private var recoverableListenFailures = 0
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
            try {
                val savedMessages = conversationRepository.messages.first()
                _state.update { current ->
                    current.copy(
                        turns = TranscriptSearch.fromMessages(savedMessages),
                        historyLoaded = true,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                _state.update { it.copy(historyLoaded = true, banner = "Saved conversation history could not be loaded.") }
            }
        }
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
            wakeWordEvents.events.collect { activateFromWakeWord(it) }
        }
    }

    fun onInputChange(value: String) = _state.update { it.copy(input = value) }

    fun send(text: String = _state.value.input, spokenInstruction: Boolean = false) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _state.value.busy || !_state.value.historyLoaded) return
        if (!_state.value.modelReady) return

        val userTurn = ChatTurn(id = System.nanoTime(), isUser = true, text = prompt)
        val assistantId = System.nanoTime() + 1
        val assistantTurn = ChatTurn(id = assistantId, isUser = false, text = "", streaming = true)
        val conversationContext = _state.value.turns.takeLast(12).map { turn ->
            (if (turn.isUser) "user" else "assistant") to turn.text
        }

        _state.update {
            it.copy(
                turns = it.turns + userTurn + assistantTurn,
                input = "",
                busy = true,
            )
        }

        runJob?.cancel()
        runJob = viewModelScope.launch {
            persistMessageSafely(prompt, ChatMessage.Role.USER)
            voice.markProcessing()
            try {
                orchestrator.run(prompt, conversationContext, spokenInstruction).collect { event ->
                    if (event is AgentEvent.Acknowledgement) {
                        event.spoken.complete(speakAcknowledgement(event.text))
                    } else {
                        applyEvent(assistantId, event)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                val message = failure.message?.takeIf(String::isNotBlank)
                    ?: "Aura couldn't reach the selected AI model. Check the connection in Settings."
                _state.update { st ->
                    st.copy(
                        banner = message,
                        turns = st.turns.map {
                            if (it.id == assistantId && it.text.isBlank()) it.copy(text = message, error = true)
                            else it
                        },
                    )
                }
            } finally {
                _state.update { st ->
                    st.copy(
                        busy = false,
                        turns = st.turns.map { if (it.id == assistantId) it.copy(streaming = false) else it },
                    )
                }
            }
            val finalText = _state.value.turns.firstOrNull { it.id == assistantId }?.text.orEmpty()
            if (finalText.isNotBlank()) persistMessageSafely(finalText, ChatMessage.Role.ASSISTANT)
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

    /** Speak exactly once on the currently selected TTS provider, before tools run. */
    private suspend fun speakAcknowledgement(text: String): Boolean {
        val completion = CompletableDeferred<Boolean>()
        val result = voice.speak(text, object : SpeechSynthesisListener {
            override fun onStarted() = Unit
            override fun onCompleted() { completion.complete(true) }
            override fun onStopped() { completion.complete(false) }
            override fun onError(error: VoiceError) {
                _state.update { it.copy(banner = error.message) }
                completion.complete(false)
            }
        })
        if (!result.accepted) {
            _state.update { it.copy(banner = result.error?.message ?: "Could not play the action acknowledgement.") }
            return false
        }
        return withTimeoutOrNull(15_000L) { completion.await() } ?: false
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
                is AgentEvent.Acknowledgement -> Unit // handled by the suspending voice path
                is AgentEvent.BackendUsed -> addTrace(
                    AgentPhase.EXECUTE,
                    if (event.provider == "cloud") "Cloud provider answered"
                    else if (event.fallbackReason != null) "Cloud unavailable; using local Ollama: " + event.fallbackReason
                    else "Using local Ollama",
                    TraceStatus.INFO,
                )
                is AgentEvent.Answer -> turn = turn.copy(
                    text = com.aura.ai.agent.ResponseAcknowledgementSanitizer.removeLeadingAcknowledgement(event.text),
                )
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

    private fun activateFromWakeWord(transcript: String?) {
        startHandsFreeSession(command = transcript?.let(com.aura.ai.voice.wake.WakePhraseMatcher::commandAfterWake))
    }

    private fun startHandsFreeSession(command: String? = null) {
        WakeWordService.stop(context)
        _state.update {
            it.copy(
                handsFreeSession = true,
                listening = command.isNullOrBlank(),
                partialSpeech = "",
                speechDiagnostic = "",
                banner = null,
            )
        }
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            state.first { it.historyLoaded }
            if (command.isNullOrBlank()) {
                startListeningTurn()
            } else {
                send(command, spokenInstruction = true)
            }
        }
    }

    private fun startListeningTurn() {
        if (!_state.value.handsFreeSession) return
        listenJob?.cancel()
        _state.update { it.copy(listening = true, partialSpeech = "") }
        val startResult = voice.startListening(
            // Aura's owner speaks South African English. Asking Android for en-US
            // caused avoidable transcript substitutions on this device.
            config = SpeechInputConfig(languageTag = "en-ZA", partialResults = true, preferOffline = false),
            listener = object : SpeechRecognitionListener {
                override fun onResult(result: SpeechRecognitionResult) {
                    val error = result.error
                    if (error != null) {
                        _state.update {
                            val heard = it.partialSpeech.trim()
                            it.copy(
                                listening = false,
                                partialSpeech = "",
                                speechDiagnostic = if (heard.isNotEmpty()) "Recognition failed after hearing: ‘$heard’" else "Recognition failed before a transcript was captured.",
                                banner = error.message,
                            )
                        }
                        if (error.code == VoiceErrorCode.AUDIO_PERMISSION_DENIED) {
                            stopHandsFreeSession()
                        } else if (error.recoverable) {
                            recoverableListenFailures = (recoverableListenFailures + 1).coerceAtMost(5)
                            val delayMs = (500L shl (recoverableListenFailures - 1)).coerceAtMost(8_000L)
                            resumeHandsFreeAfterReply(delayMs)
                        }
                    } else if (result.isFinal) {
                        recoverableListenFailures = 0
                        _state.update {
                            it.copy(
                                listening = false,
                                partialSpeech = "",
                                speechDiagnostic = "Recognized (${result.languageTag ?: "en-ZA"}, confidence ${result.confidence?.let { c -> "${(c * 100).toInt()}%" } ?: "not provided"}): ${result.transcript}",
                            )
                        }
                        if (result.transcript.isNotBlank()) send(result.transcript, spokenInstruction = true)
                        else resumeHandsFreeAfterReply()
                    } else {
                        _state.update {
                            it.copy(
                                partialSpeech = result.transcript,
                                speechDiagnostic = "Live transcript: ${result.transcript}",
                            )
                        }
                    }
                }
            },
        )
        if (!startResult.accepted) {
            _state.update {
                it.copy(
                    listening = false,
                    banner = startResult.error?.message ?: "Could not start speech recognition.",
                    speechDiagnostic = "Microphone start failed: ${startResult.error?.code ?: "unknown"}",
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
        resumeWakeWordIfEnabled()
    }

    private fun resumeWakeWordIfEnabled() {
        viewModelScope.launch {
            val wakeWordEnabled = settings.settings.first().wakeWordEnabled
            val microphoneGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (!WakeWordResumePolicy.shouldResume(wakeWordEnabled, microphoneGranted)) return@launch
            runCatching { WakeWordService.start(context) }
                .onFailure {
                    _state.update {
                        it.copy(banner = "Wake listening could not resume. Open Aura and start hands-free again.")
                    }
                }
        }
    }

    private fun resumeHandsFreeAfterReply(delayMs: Long = 350L) {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            delay(delayMs)
            startListeningTurn()
        }
    }

    fun stopSpeaking() {
        voice.stopSpeaking()
        if (_state.value.handsFreeSession) startListeningTurn()
    }

    fun dismissBanner() = _state.update { it.copy(banner = null) }

    fun setHistorySearchOpen(open: Boolean) = _state.update {
        it.copy(historySearchOpen = open, historySearchQuery = if (open) it.historySearchQuery else "")
    }

    fun setHistorySearchQuery(query: String) = _state.update { it.copy(historySearchQuery = query) }

    private suspend fun persistMessageSafely(text: String, role: ChatMessage.Role) {
        try {
            conversationRepository.saveMessage(ChatMessage(role = role, content = text))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            _state.update { it.copy(banner = "Aura couldn't save this message to conversation history.") }
        }
    }

    override fun onCleared() {
        listenJob?.cancel()
        voiceStateSubscription.close()
        super.onCleared()
    }
}
