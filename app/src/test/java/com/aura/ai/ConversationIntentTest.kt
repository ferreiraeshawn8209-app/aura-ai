package com.aura.ai

import com.aura.ai.agent.ConversationIntent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationIntentTest {
    @Test
    fun routesDirectAndPoliteActionRequestsToTheAgent() {
        listOf(
            "Yo, yo, yo, open WhatsApp",
            "Could you please call Alice?",
            "I need you to send a message to Alice",
            "Please, search the web for current business hours",
            "Check my phone for spyware",
            "Set a reminder for tomorrow",
            "Make a phone call to Alice",
        ).forEach { assertTrue("Expected action request: $it", ConversationIntent.needsAgentPlan(it)) }
    }

    @Test
    fun keepsQuestionsThatMentionTaskWordsInOrdinaryConversation() {
        listOf(
            "What does this WhatsApp message mean?",
            "How do I open WhatsApp?",
            "What is a message queue?",
            "What does this code do?",
            "Can you explain what a calendar is?",
            "How do I find a file on Windows?",
        ).forEach { assertFalse("Expected ordinary question: $it", ConversationIntent.needsAgentPlan(it)) }
    }

    @Test
    fun onlySpokenActionsNeedThePreActionAcknowledgement() {
        assertTrue(ConversationIntent.needsSpokenAcknowledgement("open WhatsApp", spokenInstruction = true))
        assertFalse(ConversationIntent.needsSpokenAcknowledgement("open WhatsApp", spokenInstruction = false))
        assertFalse(ConversationIntent.needsSpokenAcknowledgement("What does a WhatsApp message mean?", spokenInstruction = true))
    }
}
