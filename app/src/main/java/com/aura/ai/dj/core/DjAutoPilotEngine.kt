package com.aura.ai.dj.core

import com.aura.ai.dj.domain.DjAutomationState
import com.aura.ai.dj.domain.DjMode
import com.aura.ai.dj.domain.DjSession
import com.aura.ai.dj.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DjAutoPilotEngine @Inject constructor() {
    private val _automationState = MutableStateFlow(DjAutomationState.IDLE)
    val automationState: StateFlow<DjAutomationState> = _automationState.asStateFlow()

    private val _currentSession = MutableStateFlow<DjSession?>(null)
    val currentSession: StateFlow<DjSession?> = _currentSession.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _nextTrack = MutableStateFlow<Track?>(null)
    val nextTrack: StateFlow<Track?> = _nextTrack.asStateFlow()

    suspend fun startSession(
        mode: DjMode,
        duration: Long,
        initialQueue: List<Track>
    ): DjSession {
        val session = DjSession(
            id = UUID.randomUUID().toString(),
            mode = mode,
            duration = duration,
            startTime = System.currentTimeMillis(),
            queue = initialQueue,
            isRunning = true
        )
        _currentSession.emit(session)
        _automationState.emit(DjAutomationState.PLAYING)
        if (initialQueue.isNotEmpty()) {
            _currentTrack.emit(initialQueue[0])
        }
        return session
    }

    suspend fun endSession() {
        _automationState.emit(DjAutomationState.IDLE)
        _currentSession.emit(null)
        _currentTrack.emit(null)
        _nextTrack.emit(null)
    }

    suspend fun transitionToNextTrack(nextTrack: Track) {
        _nextTrack.emit(nextTrack)
        _automationState.emit(DjAutomationState.TRANSITIONING)
        // Transition logic handled by TransitionPlanner
        _currentTrack.emit(nextTrack)
        _automationState.emit(DjAutomationState.PLAYING)
    }

    suspend fun pause() {
        _automationState.emit(DjAutomationState.PAUSED)
    }

    suspend fun resume() {
        _automationState.emit(DjAutomationState.PLAYING)
    }

    fun getCurrentState(): DjAutomationState = _automationState.value
    fun isRunning(): Boolean = _currentSession.value?.isRunning ?: false
}
