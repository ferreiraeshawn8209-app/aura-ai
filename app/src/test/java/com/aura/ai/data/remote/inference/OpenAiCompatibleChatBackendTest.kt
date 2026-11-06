package com.aura.ai.data.remote.inference

import com.aura.ai.data.remote.ollama.OllamaChatMessage
import java.io.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenAiCompatibleChatBackendTest {
    private lateinit var server: MockWebServer
    private lateinit var backend: OpenAiCompatibleChatBackend

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        backend = OpenAiCompatibleChatBackend(
            OkHttpClient(),
            Json { ignoreUnknownKeys = true },
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsCompatibleRequestAndParsesAnswer() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"choices":[{"message":{"role":"assistant","content":"Affirmative."}}]}""",
            ),
        )
        val result = backend.complete(call())

        assertEquals("Affirmative.", result)
        val request = server.takeRequest()
        assertEquals("/v1/chat/completions", request.path)
        assertEquals("Bearer test-secret", request.getHeader("Authorization"))
        assertTrue(request.body.readUtf8().contains("\"model\":\"provider/model\""))
    }

    @Test
    fun providerErrorDoesNotLeakResponseBody() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("secret echo"))
        val result = runCatching { backend.complete(call()) }

        val error = result.exceptionOrNull()
        assertTrue(error is IOException)
        assertTrue(error!!.message!!.contains("HTTP 401"))
        assertFalse(error.message!!.contains("secret echo"))
    }

    @Test
    fun rejectsInsecureRemoteEndpointBeforeSendingCredentials() = runBlocking {
        val result = runCatching {
            backend.complete(call().copy(baseUrl = "http://example.test/v1"))
        }

        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals(0, server.requestCount)
    }

    private fun call() = ChatCompletionCall(
        model = "provider/model",
        messages = listOf(OllamaChatMessage("user", "hello")),
        temperature = 0.5,
        timeoutSeconds = 30,
        baseUrl = server.url("/v1").toString().trimEnd('/'),
        apiKey = "test-secret",
    )
}
