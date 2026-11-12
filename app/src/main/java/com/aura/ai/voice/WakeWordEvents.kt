package com.aura.ai.voice

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Buffers a wake event until the chat view model is ready to receive it. */
@Singleton
class WakeWordEvents @Inject constructor() {
    data class Event(val transcript: String?, val confidence: Float?, val alternatives: List<String>)

    private val channel = Channel<Event>(capacity = Channel.BUFFERED)
    val events = channel.receiveAsFlow()

    fun dispatch(transcript: String? = null, confidence: Float? = null, alternatives: List<String> = emptyList()) {
        channel.trySend(Event(transcript, confidence, alternatives))
    }
}
