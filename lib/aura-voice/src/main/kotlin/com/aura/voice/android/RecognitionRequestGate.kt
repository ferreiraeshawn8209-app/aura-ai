package com.aura.voice.android

import java.util.concurrent.atomic.AtomicLong

/**
 * Invalidates callbacks from replaced or stopped recognition requests.
 * Platform recognizer lifecycle work remains on the main thread; this gate
 * makes stale callbacks harmless immediately from any caller thread.
 */
internal class RecognitionRequestGate {
    private val generation = AtomicLong(0L)

    fun begin(): Long = generation.incrementAndGet()

    fun invalidate(): Long = generation.incrementAndGet()

    fun isCurrent(requestId: Long): Boolean = generation.get() == requestId
}
