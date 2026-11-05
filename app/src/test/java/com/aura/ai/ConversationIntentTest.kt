package com.aura.ai

import com.aura.ai.agent.ConversationIntent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationIntentTest {
    @Test
    fun ordinaryConversationUsesSingleReplyPath() {
        assertFalse(ConversationIntent.needsAgentPlan("What is five plus five?"))
        assertFalse(ConversationIntent.needsAgentPlan("Tell me something interesting"))
    }

    @Test
    fun deviceAndWorkActionsUseAgentPlan() {
        assertTrue(ConversationIntent.needsAgentPlan("Open WhatsApp"))
        assertTrue(ConversationIntent.needsAgentPlan("Search the web for my business"))
        assertTrue(ConversationIntent.needsAgentPlan("Make a call to John"))
    }
}
