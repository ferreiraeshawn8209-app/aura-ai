package com.aura.voice

import com.aura.voice.android.RecognitionTimeoutPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionTimeoutPolicyTest {
    @Test
    fun requestDoesNotExpireBeforeThirtySecondsWithoutProgress() {
        assertFalse(RecognitionTimeoutPolicy.isExpired(nowMillis = 29_999L, lastProgressMillis = 0L))
    }

    @Test
    fun requestExpiresAtThirtySecondsWithoutProgress() {
        assertTrue(RecognitionTimeoutPolicy.isExpired(nowMillis = 30_000L, lastProgressMillis = 0L))
    }

    @Test
    fun freshSpeechProgressRestartsTheTimeoutWindow() {
        assertFalse(RecognitionTimeoutPolicy.isExpired(nowMillis = 50_000L, lastProgressMillis = 25_000L))
        assertTrue(RecognitionTimeoutPolicy.isExpired(nowMillis = 55_000L, lastProgressMillis = 25_000L))
    }
}
