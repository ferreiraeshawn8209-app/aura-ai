package com.aura.voice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BargeInTranscriptPolicyTest {
    @Test
    fun requiresTwoStablePartialResultsBeforeStoppingSpeech() {
        val policy = BargeInTranscriptPolicy()

        assertFalse(policy.shouldInterrupt("Aura, stop", "Here is the answer"))
        assertTrue(policy.shouldInterrupt("Aura, stop now", "Here is the answer"))
    }

    @Test
    fun ignoresStablePartialsThatMatchAuraSpeech() {
        val policy = BargeInTranscriptPolicy()

        assertFalse(policy.shouldInterrupt("All systems", "All systems operational"))
        assertFalse(policy.shouldInterrupt("All systems operational", "All systems operational"))
    }

    @Test
    fun acceptsDifferentSpeechAfterEchoCandidateIsReset() {
        val policy = BargeInTranscriptPolicy()

        assertFalse(policy.shouldInterrupt("All systems", "All systems operational"))
        assertFalse(policy.shouldInterrupt("Actually stop", "All systems operational"))
        assertTrue(policy.shouldInterrupt("Actually stop now", "All systems operational"))
    }

    @Test
    fun recognizesPronunciationAndContractionVariantsAsEcho() {
        val policy = BargeInTranscriptPolicy()

        assertTrue(policy.isLikelyEcho("I'm on it now.", "I am on it"))
        assertTrue(policy.isLikelyEcho("All systems operational and ready.", "systems operational and ready"))
    }

    @Test
    fun doesNotDiscardSingleWordInterruptionJustBecauseAuraSpokeThatWord() {
        val policy = BargeInTranscriptPolicy()
        val spokenText = "I'm on it."

        assertFalse(policy.isLikelyEcho(spokenText, "it"))
        assertFalse(policy.shouldInterrupt("it", spokenText))
        assertTrue(policy.shouldInterrupt("it", spokenText))
    }
}
