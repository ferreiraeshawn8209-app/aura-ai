package com.aura.voice.android

/** Bounds a stalled recognition turn while allowing fresh speech results to extend it. */
internal object RecognitionTimeoutPolicy {
    const val DEFAULT_TIMEOUT_MILLIS = 30_000L

    fun isExpired(
        nowMillis: Long,
        lastProgressMillis: Long,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS,
    ): Boolean = timeoutMillis > 0 && nowMillis - lastProgressMillis >= timeoutMillis
}
