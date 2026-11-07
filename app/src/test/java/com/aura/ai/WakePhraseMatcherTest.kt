package com.aura.ai

import com.aura.ai.voice.wake.WakePhraseMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakePhraseMatcherTest {
    @Test
    fun recognizesOnlyYoYoYoAtStart() {
        assertTrue(WakePhraseMatcher.matches("Yo, yo, yo, what is five plus five?"))
        assertTrue(WakePhraseMatcher.matches("YO YO YO"))
        assertFalse(WakePhraseMatcher.matches("Hey Aura, what is five plus five?"))
        assertFalse(WakePhraseMatcher.matches("Please say yo, yo, yo"))
    }

    @Test
    fun extractsTheQuestionAfterWakePhrase() {
        assertEquals("what is five plus five?", WakePhraseMatcher.commandAfterWake("Yo, yo, yo, what is five plus five?"))
        assertEquals(null, WakePhraseMatcher.commandAfterWake("Yo, yo, yo"))
    }

    @Test
    fun keepsConfidenceAlignedWithTheAlternativeThatMatchedWakePhrase() {
        val match = WakePhraseMatcher.firstMatchingCandidate(
            transcripts = listOf("what is five plus five", "Yo yo yo, what is five plus five?"),
            confidenceScores = floatArrayOf(0.3f, 0.82f),
        )

        assertEquals("Yo yo yo, what is five plus five?", match?.transcript)
        assertEquals(0.82f, match?.confidence)
        assertEquals(listOf("what is five plus five"), match?.alternatives)
    }

    @Test
    fun omitsUnsupportedWakeConfidenceValues() {
        val match = WakePhraseMatcher.firstMatchingCandidate(
            transcripts = listOf("Yo yo yo"),
            confidenceScores = floatArrayOf(-1f),
        )

        assertEquals("Yo yo yo", match?.transcript)
        assertEquals(null, match?.confidence)
    }
}

