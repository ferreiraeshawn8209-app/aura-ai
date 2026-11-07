package com.aura.ai.ui.chat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechCommandConfidencePolicyTest {
    @Test
    fun asksForRepeatForLowConfidenceActionTranscript() {
        assertTrue(SpeechCommandConfidencePolicy.shouldAskForRepeat(0.2f, isActionRequest = true))
    }

    @Test
    fun permitsConfidentActionAndOrdinaryConversation() {
        assertFalse(SpeechCommandConfidencePolicy.shouldAskForRepeat(0.9f, isActionRequest = true))
        assertFalse(SpeechCommandConfidencePolicy.shouldAskForRepeat(0.2f, isActionRequest = false))
    }

    @Test
    fun doesNotGuessWhenRecognizerOmitsOrInvalidatesConfidence() {
        assertFalse(SpeechCommandConfidencePolicy.shouldAskForRepeat(null, isActionRequest = true))
        assertFalse(SpeechCommandConfidencePolicy.shouldAskForRepeat(-1f, isActionRequest = true))
        assertFalse(SpeechCommandConfidencePolicy.shouldAskForRepeat(1.1f, isActionRequest = true))
    }
}
