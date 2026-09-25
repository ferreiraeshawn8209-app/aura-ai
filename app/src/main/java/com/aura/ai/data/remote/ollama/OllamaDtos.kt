package com.aura.ai.data.remote.ollama

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OllamaChatMessage(
    val role: String,
    val content: String,
    val images: List<String>? = null,
)

@Serializable
data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val stream: Boolean = true,
    val options: OllamaOptions? = null,
    val format: String? = null,
    val keepAlive: String? = "10m",
)

@Serializable
data class OllamaOptions(
    val temperature: Double? = null,
    @SerialName("num_ctx") val numCtx: Int? = null,
    @SerialName("num_predict") val numPredict: Int? = null,
    val seed: Int? = null,
)

@Serializable
data class OllamaChatResponse(
    val model: String? = null,
    val message: OllamaChatMessage? = null,
    val done: Boolean = false,
    @SerialName("done_reason") val doneReason: String? = null,
    @SerialName("total_duration") val totalDuration: Long? = null,
    @SerialName("eval_count") val evalCount: Int? = null,
)

@Serializable
data class OllamaTagsResponse(
    val models: List<OllamaModel> = emptyList(),
)

@Serializable
data class OllamaModel(
    val name: String,
    val model: String? = null,
    val size: Long = 0,
    @SerialName("modified_at") val modifiedAt: String? = null,
    val details: OllamaModelDetails? = null,
)

@Serializable
data class OllamaModelDetails(
    @SerialName("parameter_size") val parameterSize: String? = null,
    @SerialName("quantization_level") val quantizationLevel: String? = null,
    val family: String? = null,
)

@Serializable
data class OllamaVersionResponse(
    val version: String,
)
