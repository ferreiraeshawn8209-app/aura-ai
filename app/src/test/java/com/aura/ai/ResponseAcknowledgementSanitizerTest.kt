package com.aura.ai

import com.aura.ai.agent.ResponseAcknowledgementSanitizer
import org.junit.Assert.assertEquals
import org.junit.Test

class ResponseAcknowledgementSanitizerTest {
    @Test
    fun removesOldOrDuplicateOpeningAcknowledgement() {
        assertEquals("The app is open.", ResponseAcknowledgementSanitizer.removeLeadingAcknowledgement("I'm on it. The app is open."))
        assertEquals("The app is open.", ResponseAcknowledgementSanitizer.removeLeadingAcknowledgement("I'll be back — The app is open."))
    }

    @Test
    fun preservesOrdinaryAnswers() {
        assertEquals("Ten.", ResponseAcknowledgementSanitizer.removeLeadingAcknowledgement("Ten."))
    }
}
