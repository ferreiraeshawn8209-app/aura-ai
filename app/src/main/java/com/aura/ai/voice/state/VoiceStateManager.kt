package com.aura.ai.voice.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class VoiceState {
    STANDBY,
    WAKE,
    LISTEN,
    SPEECH_TO_TEXT,
    AGENT,
    ACTION,
    VERIFY,
    TEXT_TO_SPEECH
}

@Singleton
class VoiceStateManager @Inject constructor() {
    private val _currentState = MutableStateFlow(VoiceState.STANDBY)
    val currentState: StateFlow<VoiceState> = _currentState.asStateFlow()

    suspend fun transitionTo(newState: VoiceState) {
        _currentState.emit(newState)
    }

    fun getCurrentState(): VoiceState = _currentState.value

    fun isProcessing(): Boolean {
        return getCurrentState() != VoiceState.STANDBY
    }
}
