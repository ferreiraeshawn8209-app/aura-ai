package com.aura.ai.data.remote.ollama

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Raw Ollama REST surface. The base URL is supplied per-call via [Url] so the
 * user can point T1000 at any reachable Ollama host (emulator loopback, LAN IP,
 * or a tunnel) without rebuilding the app.
 */
interface OllamaApi {

    @GET
    suspend fun version(@Url url: String): OllamaVersionResponse

    @GET
    suspend fun tags(@Url url: String): OllamaTagsResponse

    /** Streaming NDJSON chat. Each line is a JSON [OllamaChatResponse]. */
    @Streaming
    @POST
    suspend fun chatStream(@Url url: String, @Body body: OllamaChatRequest): ResponseBody
}
