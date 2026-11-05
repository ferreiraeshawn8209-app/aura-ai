package com.aura.ai.voice

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Buffers a wake event until the chat view model is ready to receive it. */
@Singleton
class WakeWordEvents @Inject constructor() {
    private val channel = Channel<Unit>(capacity = Channel.BUFFERED)
    val events = channel.receiveAsFlow()

    fun dispatch() {
        channel.trySend(Unit)
    }
}
