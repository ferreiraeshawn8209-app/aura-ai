package com.aura.ai.voice

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Buffers a wake event until the chat view model is ready to receive it. */
@Singleton
class WakeWordEvents @Inject constructor() {
    data class Event(val transcript: String?, val confidence: Float?, val alternatives: List<String>)

    private val channel = Channel<Event>(capacity = Channel.BUFFERED)
    private val _latestEvent = MutableStateFlow<Event?>(null)

    /** Legacy transcript-only stream consumed by the existing chat lifecycle. */
    val events = channel.receiveAsFlow().map { it.transcript }

    /** Full local recognition metadata for diagnostics and confidence policies. */
    val latestEvent: StateFlow<Event?> = _latestEvent.asStateFlow()

    fun dispatch(transcript: String? = null, confidence: Float? = null, alternatives: List<String> = emptyList()) {
        val event = Event(transcript, confidence, alternatives.toList())
        _latestEvent.value = event
        channel.trySend(event)
    }
}
