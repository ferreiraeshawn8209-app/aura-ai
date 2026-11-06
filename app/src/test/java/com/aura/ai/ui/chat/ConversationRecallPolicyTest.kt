package com.aura.ai.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationRecallPolicyTest {
    @Test
    fun selectsRelevantOlderExchangeAndPreservesItsRoles() {
        val turns = listOf(
            ChatTurn(1, true, "Can you find duplicate music tracks?"),
            ChatTurn(2, false, "I found three duplicate songs in the music folder."),
            ChatTurn(3, true, "What time is it?"),
            ChatTurn(4, false, "It is 3 PM."),
        )

        val recalled = ConversationRecallPolicy.relevantExchanges("Tell me about the duplicate songs", turns)

        assertEquals(
            listOf(
                "user" to "Can you find duplicate music tracks?",
                "assistant" to "I found three duplicate songs in the music folder.",
            ),
            recalled,
        )
    }

    @Test
    fun ignoresUnmatchedIncompleteAndFailedExchanges() {
        val turns = listOf(
            ChatTurn(1, true, "Find duplicate music tracks"),
            ChatTurn(2, false, "I found three duplicates", error = true),
            ChatTurn(3, true, "Find old project files"),
        )

        assertTrue(ConversationRecallPolicy.relevantExchanges("Tell me about music duplicates", turns).isEmpty())
    }

    @Test
    fun capsOlderExchangesAndTotalContextSize() {
        val turns = (1L..8L).flatMap { id ->
            listOf(
                ChatTurn(id * 2, true, "Review the Aura project memory number $id."),
                ChatTurn(id * 2 + 1, false, "Aura project memory details ".repeat(50)),
            )
        }

        val recalled = ConversationRecallPolicy.relevantExchanges(
            request = "What did we discuss about the Aura project memory?",
            olderTurns = turns,
            maxExchanges = 2,
            maxCharacters = 1_800,
        )

        assertEquals(4, recalled.size)
        assertTrue(recalled.sumOf { it.second.length } <= 1_800)
        assertTrue(recalled.all { it.second.length <= 900 })
    }
}
