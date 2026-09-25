package com.aura.ai.dj.queue

import com.aura.ai.dj.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class QueueManager @Inject constructor() {
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    suspend fun setQueue(tracks: List<Track>) {
        _queue.emit(tracks)
        _currentIndex.emit(0)
    }

    suspend fun addTrackToQueue(track: Track) {
        val current = _queue.value.toMutableList()
        current.add(track)
        _queue.emit(current)
    }

    suspend fun removeTrackFromQueue(index: Int) {
        val current = _queue.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _queue.emit(current)
        }
    }

    suspend fun moveToNextTrack() {
        val nextIndex = (_currentIndex.value + 1).coerceAtMost(_queue.value.size - 1)
        _currentIndex.emit(nextIndex)
    }

    suspend fun moveToPreviousTrack() {
        val prevIndex = (_currentIndex.value - 1).coerceAtLeast(0)
        _currentIndex.emit(prevIndex)
    }

    fun getCurrentTrack(): Track? {
        val index = _currentIndex.value
        return if (index in _queue.value.indices) _queue.value[index] else null
    }

    fun getNextTrack(): Track? {
        val index = _currentIndex.value + 1
        return if (index in _queue.value.indices) _queue.value[index] else null
    }

    fun getQueue(): List<Track> = _queue.value
    fun getCurrentIndex(): Int = _currentIndex.value
}
