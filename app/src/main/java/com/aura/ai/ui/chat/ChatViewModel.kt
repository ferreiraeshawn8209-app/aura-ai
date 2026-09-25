package com.aura.ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.agent.AgentEvent
import com.aura.ai.agent.AgentOrchestrator
import com.aura.ai.agent.AgentPhase
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.voice.Speaker
import com.aura.ai.voice.SpeechEvent
import com.aura.ai.voice.SpeechToText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: AgentOrchestrator,
    private val settings: SettingsRepository,
    private val speech: SpeechToText,
    private val speaker: Speaker,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var runJob: Job? = null
    private var listenJob: Job? = null

    init {
        speaker.init()
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
            speaker.isSpeaking.collect { sp -> _state.update { it.copy(speaking = sp) } }
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
            if (voiceOn && finalText.isNotBlank()) speaker.speak(finalText)
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
        if (_state.value.listening) stopListening() else startListening()
    }

    private fun startListening() {
        if (!speech.isAvailable()) {
            _state.update { it.copy(banner = "Speech recognition isn't available on this device.") }
            return
        }
        speaker.stop()
        _state.update { it.copy(listening = true, partialSpeech = "") }
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            speech.listen().collect { ev ->
                when (ev) {
                    is SpeechEvent.Partial -> _state.update { it.copy(partialSpeech = ev.text) }
                    is SpeechEvent.Final -> {
                        _state.update { it.copy(listening = false, partialSpeech = "") }
                        if (ev.text.isNotBlank()) send(ev.text)
                    }
                    is SpeechEvent.Error -> _state.update {
                        it.copy(listening = false, partialSpeech = "", banner = ev.message)
                    }
                    SpeechEvent.EndOfSpeech -> _state.update { it.copy(listening = false) }
                    else -> Unit
                }
            }
        }
    }

    private fun stopListening() {
        listenJob?.cancel()
        _state.update { it.copy(listening = false, partialSpeech = "") }
    }

    fun stopSpeaking() = speaker.stop()

    fun dismissBanner() = _state.update { it.copy(banner = null) }

    override fun onCleared() {
        speaker.shutdown()
        super.onCleared()
    }
}
