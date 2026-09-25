package com.aura.ai.data.remote

import com.aura.ai.domain.model.OllamaChatRequest
import com.aura.ai.domain.model.OllamaChatResponse
import com.aura.ai.domain.model.OllamaModelsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OllamaApi {
    @GET("api/tags")
    suspend fun listModels(): OllamaModelsResponse

    @POST("api/chat")
    suspend fun chat(@Body request: OllamaChatRequest): OllamaChatResponse

    @GET("api/version")
    suspend fun getVersion(): Map<String, String>
}
