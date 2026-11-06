package com.aura.ai.voice.wake

/** Keeps ordinary quiet-room timeouts from creating long wake-listening gaps. */
internal object WakeRestartPolicy {
    enum class Cause { NORMAL, SPEECH_TIMEOUT, NO_MATCH, RECOGNIZER_FAILURE }

    data class Decision(val delayMillis: Long, val consecutiveErrors: Int)

    fun decide(cause: Cause, previousErrors: Int): Decision {
        if (cause != Cause.RECOGNIZER_FAILURE) {
            return Decision(NORMAL_RESTART_DELAY_MILLIS, consecutiveErrors = 0)
        }
        val errors = (previousErrors + 1).coerceAtMost(MAX_CONSECUTIVE_ERRORS)
        val delay = (ERROR_RESTART_BASE_MILLIS * (1 shl (errors - 1)))
            .coerceAtMost(MAX_ERROR_RESTART_DELAY_MILLIS)
        return Decision(delay, errors)
    }

    const val NORMAL_RESTART_DELAY_MILLIS = 800L
    const val ERROR_RESTART_BASE_MILLIS = 1_000L
    const val MAX_ERROR_RESTART_DELAY_MILLIS = 16_000L
    const val MAX_CONSECUTIVE_ERRORS = 5
}
