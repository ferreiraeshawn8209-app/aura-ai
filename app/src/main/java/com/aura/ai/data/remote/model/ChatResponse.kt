package com.aura.ai.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "id") val id: String,
    @Json(name = "choices") val choices: List<Choice>,
    @Json(name = "usage") val usage: Usage? = null
)

@JsonClass(generateAdapter = true)
data class Choice(
    @Json(name = "index") val index: Int,
    @Json(name = "message") val message: MessageDto,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int,
    @Json(name = "completion_tokens") val completionTokens: Int,
    @Json(name = "total_tokens") val totalTokens: Int
)
