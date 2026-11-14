package com.aura.ai.voice.wake

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WakePhraseMatcherTest {
    @Test
    fun matchesTheRequestedWakePhraseAndKeepsCommandAfterIt() {
        assertTrue(WakePhraseMatcher.matches("Yo, yo, yo, what is five plus five?"))
        assertEquals(
            "what is five plus five?",
            WakePhraseMatcher.commandAfterWake("Yo, yo, yo, what is five plus five?"),
        )
    }

    @Test
    fun acceptsCommonOfflineRecognitionHomophonesForTheWakeTokens() {
        assertTrue(WakePhraseMatcher.matches("Yoh, yo, you, open WhatsApp"))
        assertTrue(WakePhraseMatcher.matches("yeah ya yoo"))
        assertTrue(WakePhraseMatcher.matches("yoyoyo, tell me the time"))
    }

    @Test
    fun selectsTheFirstMatchingRecognitionAlternativeAndItsConfidence() {
        val match = WakePhraseMatcher.firstMatchingCandidate(
            transcripts = listOf("you said no", "you, yoh, yo, what time is it?"),
            confidenceScores = floatArrayOf(0.23f, 0.82f),
        )

        assertNotNull(match)
        assertEquals("you, yoh, yo, what time is it?", match?.transcript)
        assertEquals(0.82f, match?.confidence ?: 0f, 0.001f)
    }

    @Test
    fun rejectsIncompleteOrNonLeadingWakePhrases() {
        assertFalse(WakePhraseMatcher.matches("yo yo"))
        assertFalse(WakePhraseMatcher.matches("please say yo yo yo"))
        assertNull(WakePhraseMatcher.commandAfterWake("yo yo"))
    }

    @Test
    fun commandAfterWakeReturnsNullWhenThereIsNoSpokenCommand() {
        assertNull(WakePhraseMatcher.commandAfterWake("yo, yo, yo"))
    }
}
