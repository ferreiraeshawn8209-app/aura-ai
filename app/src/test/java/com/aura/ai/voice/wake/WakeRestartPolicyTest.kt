package com.aura.ai.voice.wake

import org.junit.Assert.assertEquals
import org.junit.Test

class WakeRestartPolicyTest {
    @Test
    fun ordinarySilenceAndNoMatchKeepWakeGapsShortAndResetErrorCount() {
        for (cause in listOf(WakeRestartPolicy.Cause.NORMAL, WakeRestartPolicy.Cause.SPEECH_TIMEOUT, WakeRestartPolicy.Cause.NO_MATCH)) {
            val decision = WakeRestartPolicy.decide(cause, previousErrors = 5)
            assertEquals(WakeRestartPolicy.NORMAL_RESTART_DELAY_MILLIS, decision.delayMillis)
            assertEquals(0, decision.consecutiveErrors)
        }
    }

    @Test
    fun repeatedRecognizerFailuresUseCappedBackoff() {
        val expectedDelays = listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 16_000L)
        var errors = 0

        expectedDelays.forEach { expected ->
            val decision = WakeRestartPolicy.decide(WakeRestartPolicy.Cause.RECOGNIZER_FAILURE, errors)
            assertEquals(expected, decision.delayMillis)
            errors = decision.consecutiveErrors
        }
    }
}
