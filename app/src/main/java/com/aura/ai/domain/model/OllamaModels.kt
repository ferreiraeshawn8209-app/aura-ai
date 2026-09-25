package com.aura.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OllamaModel(
    val name: String,
    val modified: String,
    val size: Long,
    val digest: String
)

@Serializable
data class OllamaModelsResponse(
    val models: List<OllamaModel>
)

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val stream: Boolean = false
)

@Serializable
data class OllamaChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class OllamaChatResponse(
    val model: String,
    val created_at: String,
    val message: OllamaChatMessage,
    val done: Boolean,
    val total_duration: Long? = null,
    val load_duration: Long? = null,
    val prompt_eval_count: Int? = null,
    val prompt_eval_duration: Long? = null,
    val eval_count: Int? = null,
    val eval_duration: Long? = null
)

enum class OllamaConnectionState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR,
    MODEL_UNAVAILABLE,
    STREAMING,
    CANCELLED
}
