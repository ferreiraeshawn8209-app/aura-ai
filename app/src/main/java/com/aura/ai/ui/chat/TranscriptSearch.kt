package com.aura.ai.ui.chat

import com.aura.ai.data.model.ChatMessage

internal object TranscriptSearch {
    fun fromMessages(messages: List<ChatMessage>): List<ChatTurn> = messages.map { message ->
        ChatTurn(
            id = message.id,
            isUser = message.role == ChatMessage.Role.USER,
            text = message.content,
            error = message.role == ChatMessage.Role.SYSTEM,
        )
    }

    fun filter(turns: List<ChatTurn>, query: String): List<ChatTurn> {
        val normalized = query.trim()
        if (normalized.isEmpty()) return turns
        return turns.filter { it.text.contains(normalized, ignoreCase = true) }
    }
}
