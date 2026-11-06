package com.aura.ai.ui.chat

import com.aura.ai.data.model.ChatMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class TranscriptSearchTest {
    private val turns = listOf(
        ChatTurn(id = 1, isUser = true, text = "Check the music folder"),
        ChatTurn(id = 2, isUser = false, text = "I found three duplicate tracks"),
        ChatTurn(id = 3, isUser = true, text = "Thanks"),
    )

    @Test
    fun savedMessagesMapBackToTheirTranscriptTurns() {
        val messages = listOf(
            ChatMessage(id = 11, role = ChatMessage.Role.USER, content = "What time is it?"),
            ChatMessage(id = 12, role = ChatMessage.Role.ASSISTANT, content = "It is noon."),
        )

        assertEquals(
            listOf(
                ChatTurn(id = 11, isUser = true, text = "What time is it?"),
                ChatTurn(id = 12, isUser = false, text = "It is noon."),
            ),
            TranscriptSearch.fromMessages(messages),
        )
    }

    @Test
    fun blankQueryReturnsAllTurnsInOriginalOrder() {
        assertEquals(turns, TranscriptSearch.filter(turns, "  "))
    }

    @Test
    fun searchIsCaseInsensitiveAndKeepsOnlyMatchingTranscriptText() {
        assertEquals(
            listOf(turns[1]),
            TranscriptSearch.filter(turns, "DUPLICATE"),
        )
    }
}
