package com.aura.ai.data.remote.inference

import com.aura.ai.data.remote.ollama.ChatChunk
import com.aura.ai.data.remote.ollama.OllamaChatMessage
import com.aura.ai.data.remote.ollama.OllamaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ChatCompletionCall(
    val model: String,
    val messages: List<OllamaChatMessage>,
    val temperature: Double,
    val timeoutSeconds: Int,
    val baseUrl: String? = null,
    val apiKey: String? = null,
)

data class ChatCompletionResult(
    val text: String,
    val provider: String,
    val fallbackReason: String? = null,
)

interface ChatCompletionBackend {
    suspend fun complete(call: ChatCompletionCall): String
}

class OllamaChatBackend(private val ollama: OllamaRepository) : ChatCompletionBackend {
    override suspend fun complete(call: ChatCompletionCall): String {
        val response = StringBuilder()
        var failure: Throwable? = null
        ollama.chat(
            model = call.model,
            messages = call.messages,
            temperature = call.temperature,
            timeoutSeconds = call.timeoutSeconds,
        ).collect { chunk ->
            when (chunk) {
                is ChatChunk.Delta -> response.append(chunk.text)
                is ChatChunk.Done -> Unit
                is ChatChunk.Error -> failure = IOException(chunk.error.message)
            }
        }
        failure?.let { throw it }
        return response.toString().takeIf(String::isNotBlank)
            ?: throw IOException("Ollama returned an empty response.")
    }
}

@Serializable
private data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Double,
    @kotlinx.serialization.SerialName("max_tokens") val maxTokens: Int = 768,
    val stream: Boolean = false,
)

@Serializable
private data class OpenAiMessage(val role: String, val content: String)

class OpenAiCompatibleChatBackend(
    private val client: OkHttpClient,
    private val json: Json,
) : ChatCompletionBackend {
    override suspend fun complete(call: ChatCompletionCall): String {
        val apiKey = call.apiKey?.trim().orEmpty()
        val baseUrl = call.baseUrl?.trim().orEmpty()
        require(call.model.isNotBlank()) { "Cloud model is not configured." }
        require(apiKey.isNotBlank()) { "Cloud API key is not configured." }
        val completionUrl = endpoint(baseUrl)
        val requestBody = json.encodeToString(
            OpenAiRequest(
                model = call.model,
                messages = call.messages.map { OpenAiMessage(it.role, it.content) },
                temperature = call.temperature,
            ),
        ).toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(completionUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
        val response = withTimeout(TimeUnit.SECONDS.toMillis(call.timeoutSeconds.coerceIn(10, 600).toLong())) {
            client.newCall(request).awaitResponse()
        }
        response.use {
            if (!it.isSuccessful) {
                // Provider bodies may contain request data; do not expose them.
                throw IOException("Cloud provider returned HTTP ${it.code}.")
            }
            val body = it.body ?: throw IOException("Cloud provider returned an empty body.")
            if (body.contentLength() > MAX_RESPONSE_BYTES) {
                throw IOException("Cloud provider response exceeded the 2 MB limit.")
            }
            val root = runCatching { json.parseToJsonElement(body.string()) as? JsonObject }.getOrNull()
                ?: throw IOException("Cloud provider returned malformed JSON.")
            val choices = root["choices"] as? JsonArray
                ?: throw IOException("Cloud provider response did not include choices.")
            val message = choices.firstOrNull()?.let { it as? JsonObject }?.get("message") as? JsonObject
                ?: throw IOException("Cloud provider response did not include a message.")
            val content = (message["content"] as? JsonPrimitive)?.contentOrNull
                ?.takeIf(String::isNotBlank)
                ?: throw IOException("Cloud provider returned an empty answer.")
            return content
        }
    }

    private fun endpoint(baseUrl: String): String {
        val parsed = baseUrl.toHttpUrlOrNull()
            ?: throw IOException("Cloud endpoint must be a valid HTTPS URL.")
        if (parsed.scheme != "https" && parsed.host !in LOCAL_HTTP_HOSTS) {
            throw IOException("Cloud endpoint must use HTTPS.")
        }
        val clean = baseUrl.trimEnd('/')
        return if (clean.endsWith("/chat/completions")) clean else "$clean/chat/completions"
    }

    private companion object {
        const val MAX_RESPONSE_BYTES = 2L * 1024L * 1024L
        val LOCAL_HTTP_HOSTS = setOf("localhost", "127.0.0.1", "10.0.2.2")
    }
}

@Singleton
class ProviderChatRouter @Inject constructor(
    @Named("cloudChat") private val cloudBackend: ChatCompletionBackend,
    @Named("localChat") private val localBackend: ChatCompletionBackend,
) {
    private var breaker = CloudCircuitBreaker()

    internal constructor(
        cloudBackend: ChatCompletionBackend,
        localBackend: ChatCompletionBackend,
        breaker: CloudCircuitBreaker,
    ) : this(cloudBackend, localBackend) {
        this.breaker = breaker
    }

    suspend fun complete(
        local: ChatCompletionCall,
        cloud: ChatCompletionCall?,
        cloudUnavailableReason: String? = null,
    ): ChatCompletionResult {
        var fallbackReason: String? = cloudUnavailableReason
        if (cloud != null) {
            if (cloud.model.isBlank() || cloud.baseUrl.isNullOrBlank() || cloud.apiKey.isNullOrBlank()) {
                fallbackReason = "Cloud provider settings are incomplete."
            } else if (breaker.allow()) {
                try {
                    val answer = cloudBackend.complete(cloud)
                    breaker.succeeded()
                    return ChatCompletionResult(answer, "cloud")
                } catch (timeout: TimeoutCancellationException) {
                    fallbackReason = "Cloud provider timed out."
                    breaker.failed()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    fallbackReason = failure.message ?: "Cloud provider request failed."
                    breaker.failed()
                }
            } else {
                fallbackReason = "Cloud provider is cooling down after a recent failure."
            }
        }

        return try {
            ChatCompletionResult(localBackend.complete(local), "local", fallbackReason)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            val detail = fallbackReason?.let { "$it Local Ollama also failed." } ?: "Local Ollama failed."
            throw ProviderRoutingException(detail, failure)
        }
    }
}

class ProviderRoutingException(message: String, cause: Throwable? = null) : Exception(message, cause)

internal class CloudCircuitBreaker(
    private val cooldownMillis: Long = DEFAULT_COOLDOWN_MILLIS,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private var openUntilMillis: Long = 0

    @Synchronized
    fun allow(): Boolean = nowMillis() >= openUntilMillis

    @Synchronized
    fun failed() {
        openUntilMillis = nowMillis() + cooldownMillis
    }

    @Synchronized
    fun succeeded() {
        openUntilMillis = 0
    }

    internal companion object {
        const val DEFAULT_COOLDOWN_MILLIS = 30_000L
    }
}

private suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isActive) continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            if (continuation.isActive) continuation.resume(response) else response.close()
        }
    })
}
