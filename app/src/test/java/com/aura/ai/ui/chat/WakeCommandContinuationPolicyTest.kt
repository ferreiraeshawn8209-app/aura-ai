package com.aura.ai.ui.chat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeCommandContinuationPolicyTest {
    @Test
    fun continuesAfterClearlyIncompleteWakeCommandFragments() {
        listOf("I", "what is", "what are?", "how do", "can you", "open").forEach {
            assertTrue("Expected continuation for: $it", WakeCommandContinuationPolicy.shouldContinue(it))
        }
    }

    @Test
    fun sendsCompleteQuestionsAndCommandsWithoutAnExtraListeningTurn() {
        listOf(
            "what is five plus five?",
            "I want to know the time",
            "open WhatsApp",
            "search for Aura voice recognition",
            "yes",
        ).forEach {
            assertFalse("Expected complete request: $it", WakeCommandContinuationPolicy.shouldContinue(it))
        }
    }

    @Test
    fun normalizesCaseWhitespaceAndTrailingPunctuation() {
        assertTrue(WakeCommandContinuationPolicy.shouldContinue("  WHAT   IS?  "))
        assertTrue(WakeCommandContinuationPolicy.shouldContinue("what’s"))
    }
}
