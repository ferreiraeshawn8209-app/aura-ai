package com.aura.ai.agent

import com.aura.ai.data.local.entity.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentPromptContextTest {
    @Test
    fun taskUnderstandingReceivesConversationContextAndCurrentRequestInOrder() {
        val messages = AgentPrompts.understand(
            request = "Open that website",
            memories = listOf(MemoryEntity(type = "PROJECT_CONTEXT", content = "The company site is example.org")),
            conversationContext = listOf("user" to "Check my company website", "assistant" to "Which one?"),
        )

        assertEquals(listOf("system", "user", "assistant", "user"), messages.map { it.first })
        assertTrue(messages.first().second.contains("The company site is example.org"))
        assertEquals("Check my company website", messages[1].second)
        assertEquals("Open that website", messages.last().second)
    }
}
