package com.aura.ai.voice.wake

import java.util.concurrent.atomic.AtomicLong

/** Makes callbacks from replaced or stopped wake recognizers harmless. */
internal class WakeWordRequestGate {
    private val generation = AtomicLong(0L)

    fun begin(): Long = generation.incrementAndGet()

    fun invalidate(): Long = generation.incrementAndGet()

    fun isCurrent(requestId: Long): Boolean = generation.get() == requestId
}
