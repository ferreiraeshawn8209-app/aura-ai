package com.aura.ai.data.model

/**
 * Represents a single message in the conversation between the user and AURA.
 */
data class ChatMessage(
    val id: Long = 0L,
    val role: Role,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Role { USER, ASSISTANT, SYSTEM }
}
