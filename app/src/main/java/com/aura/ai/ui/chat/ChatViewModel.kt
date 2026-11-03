package com.aura.ai.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.agent.AgentEvent
import com.aura.ai.agent.AgentOrchestrator
import com.aura.ai.agent.AgentPhase
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.data.repository.ConversationRepository
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.voice.Speaker
import com.aura.ai.voice.SpeechEvent
import com.aura.ai.voice.SpeechToText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val speech: SpeechToText,
    private val speaker: Speaker,
    private val conversations: ConversationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var runJob: Job? = null
    private var listenJob: Job? = null

    init {
        speaker.init()
        viewModelScope.launch {
            settings.settings.collect { config ->
                _state.update {
                    it.copy(
                        modelReady = !config.selectedModel.isNullOrBlank(),
                        modelName = config.selectedModel,
                        banner = if (config.selectedModel.isNullOrBlank())
                            "No model selected. Open Settings to connect Ollama and pick a model." else null,
                    )
                }
            }
        }
        // Hydrate the visible transcript once. We intentionally don't collect it continuously,
        // because doing so would overwrite in-progress streamed agent traces.
        viewModelScope.launch {
            val saved = conversations.messages.first()
            if (_state.value.turns.isEmpty() && saved.isNotEmpty()) {
                _state.update { current ->
                    if (current.turns.isNotEmpty()) current else current.copy(
                        turns = saved.map { message ->
                            ChatTurn(
                                id = message.id,
                                isUser = message.role == ChatMessage.Role.USER,
                                text = message.content,
                            )
                        },
                    )
                }
            }
        }
        viewModelScope.launch {
            speaker.isSpeaking.collect { speaking -> _state.update { it.copy(speaking = speaking) } }
        }
    }

    fun onInputChange(value: String) = _state.update { it.copy(input = value) }

    fun send(text: String = _state.value.input) {
        val prompt = text.trim()
        if (prompt.isEmpty() || _state.value.busy || !_state.value.modelReady) return

        val userTurn = ChatTurn(id = System.nanoTime(), isUser = true, text = prompt)
        val assistantId = System.nanoTime() + 1
        _state.update { it.copy(turns = it.turns + userTurn + ChatTurn(assistantId, false, "", streaming = true), input = "", busy = true) }

        runJob?.cancel()
        runJob = viewModelScope.launch {
            conversations.saveMessage(ChatMessage(role = ChatMessage.Role.USER, content = prompt))
            orchestrator.run(prompt).collect { event -> applyEvent(assistantId, event) }
            val finalText = _state.value.turns.firstOrNull { it.id == assistantId }?.text.orEmpty()
            _state.update { current ->
                current.copy(
                    busy = false,
                    turns = current.turns.map { if (it.id == assistantId) it.copy(streaming = false) else it },
                )
            }
            if (finalText.isNotBlank()) {
                conversations.saveMessage(ChatMessage(role = ChatMessage.Role.ASSISTANT, content = finalText))
                if (settings.settings.first().voiceRepliesEnabled) speaker.speak(finalText)
            }
        }
    }

    private fun applyEvent(assistantId: Long, event: AgentEvent) {
        _state.update { current ->
            val turns = current.turns.toMutableList()
            val index = turns.indexOfFirst { it.id == assistantId }
            if (index < 0) return@update current
            var turn = turns[index]

            fun addTrace(phase: AgentPhase, label: String, status: TraceStatus) {
                turn = turn.copy(trace = turn.trace + TraceLine(phase, label, status))
            }

            when (event) {
                is AgentEvent.Phase -> addTrace(event.phase, event.detail, TraceStatus.ACTIVE)
                is AgentEvent.Understood -> addTrace(AgentPhase.UNDERSTAND, "Goal: ${event.understanding.goal}", TraceStatus.OK)
                is AgentEvent.Planned -> addTrace(
                    AgentPhase.PLAN,
                    "${event.plan.steps.size} step plan" + if (event.reusedSkill) " (reused a learned skill)" else "",
                    TraceStatus.OK,
                )
                is AgentEvent.StepStarted -> addTrace(AgentPhase.EXECUTE, "→ ${event.step.description}", TraceStatus.ACTIVE)
                is AgentEvent.StepObserved -> addTrace(
                    AgentPhase.EXECUTE,
                    if (event.observation.success) "✓ step ${event.observation.stepIndex}" else "✗ ${event.observation.error}",
                    if (event.observation.success) TraceStatus.OK else TraceStatus.FAIL,
                )
                is AgentEvent.Token -> turn = turn.copy(text = turn.text + event.text)
                is AgentEvent.Verified -> addTrace(
                    AgentPhase.VERIFY,
                    if (event.verdict.verified) "Verified against all criteria" else "Not verified: ${event.verdict.unmetCriteria.joinToString("; ")}",
                    if (event.verdict.verified) TraceStatus.OK else TraceStatus.FAIL,
                )
                is AgentEvent.Replan -> addTrace(AgentPhase.PLAN, "Re-planning (attempt ${event.attempt + 1})", TraceStatus.INFO)
                is AgentEvent.Learned -> addTrace(AgentPhase.LEARN, event.note, TraceStatus.INFO)
                is AgentEvent.Answer -> if (turn.text.isBlank()) turn = turn.copy(text = event.text)
                is AgentEvent.Failed -> turn = turn.copy(text = if (turn.text.isBlank()) event.message else turn.text, error = true)
            }
            turns[index] = turn
            current.copy(turns = turns)
        }
    }

    fun toggleListening() {
        if (_state.value.listening) stopListening() else startListening()
    }

    private fun startListening() {
        if (!speech.isAvailable()) {
            _state.update { it.copy(banner = "Speech recognition isn't available on this device.") }
            return
        }
        // Starting speech input is an explicit interruption gesture. Stop both TTS and an
        // in-flight Ollama generation before opening the microphone.
        speaker.stop()
        if (_state.value.busy) {
            runJob?.cancel()
            runJob = null
            _state.update { current ->
                current.copy(
                    busy = false,
                    turns = current.turns.map { if (it.streaming) it.copy(streaming = false) else it },
                )
            }
        }
        _state.update { it.copy(listening = true, partialSpeech = "") }
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            speech.listen().collect { event ->
                when (event) {
                    is SpeechEvent.Partial -> _state.update { it.copy(partialSpeech = event.text) }
                    is SpeechEvent.Final -> {
                        _state.update { it.copy(listening = false, partialSpeech = "") }
                        if (event.text.isNotBlank()) send(event.text)
                    }
                    is SpeechEvent.Error -> _state.update { it.copy(listening = false, partialSpeech = "", banner = event.message) }
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
