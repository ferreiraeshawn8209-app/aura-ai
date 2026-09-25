package com.aura.ai.data.remote.ollama

import com.aura.ai.data.settings.SettingsRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single gateway to Ollama. Every entry point returns typed results so
 * callers never have to interpret raw exceptions. Ollama is the only inference
 * backend — there is no cloud fallback.
 */
@Singleton
class OllamaRepository @Inject constructor(
    private val api: OllamaApi,
    private val settings: SettingsRepository,
    private val json: Json,
) {
    private suspend fun baseUrl(): String = settings.settings.first().ollamaBaseUrl

    /** Verify the host answers and report its version. */
    suspend fun testConnection(baseUrl: String? = null): OllamaResult<String> {
        val host = (baseUrl ?: baseUrl())
        return try {
            val version = api.version("$host/api/version")
            OllamaResult.Success(version.version)
        } catch (e: IOException) {
            OllamaResult.Failure(OllamaError.Unreachable(host, e.message ?: "no route"))
        } catch (e: HttpException) {
            OllamaResult.Failure(OllamaError.Http(e.code(), e.message()))
        }
    }

    /** List models actually installed on the server. */
    suspend fun listModels(baseUrl: String? = null): OllamaResult<List<OllamaModel>> {
        val host = (baseUrl ?: baseUrl())
        return try {
            OllamaResult.Success(api.tags("$host/api/tags").models.sortedBy { it.name })
        } catch (e: IOException) {
            OllamaResult.Failure(OllamaError.Unreachable(host, e.message ?: "no route"))
        } catch (e: HttpException) {
            OllamaResult.Failure(OllamaError.Http(e.code(), e.message()))
        }
    }

    /**
     * Stream a chat completion token-by-token. Emits [ChatChunk] deltas and a
     * terminal [ChatChunk.Done]. Errors are emitted as [ChatChunk.Error] and the
     * flow completes normally, so the UI layer stays simple.
     */
    fun chat(
        model: String,
        messages: List<OllamaChatMessage>,
        temperature: Double,
        timeoutSeconds: Int,
        numCtx: Int = 8192,
    ): Flow<ChatChunk> = flow {
        val host = baseUrl()
        val request = OllamaChatRequest(
            model = model,
            messages = messages,
            stream = true,
            options = OllamaOptions(temperature = temperature, numCtx = numCtx),
        )
        try {
            withTimeout(timeoutSeconds * 1000L) {
                val body = api.chatStream("$host/api/chat", request)
                body.byteStream().bufferedReader().use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        if (line.isNotBlank()) {
                            val chunk = runCatching {
                                json.decodeFromString(OllamaChatResponse.serializer(), line)
                            }.getOrNull()
                            if (chunk == null) {
                                emit(ChatChunk.Error(OllamaError.Malformed(line.take(200))))
                                return@use
                            }
                            chunk.message?.content?.let { if (it.isNotEmpty()) emit(ChatChunk.Delta(it)) }
                            if (chunk.done) {
                                emit(ChatChunk.Done(chunk.evalCount ?: 0))
                                return@use
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            emit(ChatChunk.Error(OllamaError.Timeout(timeoutSeconds)))
        } catch (e: HttpException) {
            val msg = e.response()?.errorBody()?.string().orEmpty()
            if (e.code() == 404 || msg.contains("not found", ignoreCase = true)) {
                emit(ChatChunk.Error(OllamaError.ModelNotFound(model)))
            } else {
                emit(ChatChunk.Error(OllamaError.Http(e.code(), msg)))
            }
        } catch (e: IOException) {
            emit(ChatChunk.Error(OllamaError.Unreachable(host, e.message ?: "connection lost")))
        }
    }.flowOn(Dispatchers.IO)
}

sealed interface ChatChunk {
    data class Delta(val text: String) : ChatChunk
    data class Done(val evalCount: Int) : ChatChunk
    data class Error(val error: OllamaError) : ChatChunk
}
