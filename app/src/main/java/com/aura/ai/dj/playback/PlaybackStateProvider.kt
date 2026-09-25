package com.aura.ai.dj.playback

import com.aura.ai.dj.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PlaybackStateProvider @Inject constructor() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    suspend fun updatePlaybackState(isPlaying: Boolean) {
        _isPlaying.emit(isPlaying)
    }

    suspend fun updatePosition(position: Long) {
        _currentPosition.emit(position)
    }

    suspend fun setCurrentTrack(track: Track?) {
        _currentTrack.emit(track)
    }

    fun getIsPlaying(): Boolean = _isPlaying.value
    fun getCurrentPosition(): Long = _currentPosition.value
    fun getCurrentTrack(): Track? = _currentTrack.value
}
