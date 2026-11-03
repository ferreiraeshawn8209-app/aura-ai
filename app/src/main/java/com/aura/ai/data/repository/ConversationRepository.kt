package com.aura.ai.data.repository

import com.aura.ai.data.database.dao.ConversationDao
import com.aura.ai.data.database.entity.ConversationEntity
import com.aura.ai.data.model.ChatMessage
import com.aura.ai.domain.ai.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val aiProvider: AiProvider,
) {
    /** Stream all saved messages in chronological order. */
    val messages: Flow<List<ChatMessage>> = conversationDao.getAllMessages().map { entities ->
        entities.map(ConversationEntity::toChatMessage)
    }

    /** Search saved transcripts by message text (case-insensitive on SQLite's default LIKE collation). */
    fun searchMessages(query: String, limit: Int = 100): Flow<List<ChatMessage>> =
        conversationDao.searchMessages(query.trim(), limit.coerceIn(1, 500)).map { entities ->
            entities.map(ConversationEntity::toChatMessage)
        }

    suspend fun saveMessage(message: ChatMessage): Long = conversationDao.insert(
        ConversationEntity(
            role = message.role.name.lowercase(),
            content = message.content,
            timestamp = message.timestamp,
        ),
    )

    suspend fun sendToOllama(systemPrompt: String, history: List<ChatMessage>): String =
        aiProvider.generate(systemPrompt, history).getOrThrow()

    suspend fun clearHistory() = conversationDao.clearAll()

    private fun ConversationEntity.toChatMessage() = ChatMessage(
        id = id,
        role = when (role.uppercase()) {
            "USER" -> ChatMessage.Role.USER
            "ASSISTANT" -> ChatMessage.Role.ASSISTANT
            else -> ChatMessage.Role.SYSTEM
        },
        content = content,
        timestamp = timestamp,
    )
}
