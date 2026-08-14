package com.aura.ai

import com.aura.ai.network.RedactingLoggingInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Test

class OkHttpRedactionTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun logsDoNotContainApiKeyOrAuthorization() {
        val logger = TestHttpLogger()
        val interceptor = RedactingLoggingInterceptor(logger)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer "+BuildConfig.OPENAI_API_KEY)
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(interceptor)
            .build()

        server.enqueue(MockResponse().setBody("hello"))

        val request = Request.Builder()
            .url(server.url("/test"))
            .get()
            .build()

        client.newCall(request).execute().use { resp ->
            // consume
            resp.body?.string()
        }

        val joined = logger.lines.joinToString("\n")

        // Ensure the API key is not present in logs
        assertFalse("API key leaked into logs", joined.contains(BuildConfig.OPENAI_API_KEY))
        // Ensure Authorization header is not shown
        assertFalse("Authorization header leaked into logs", joined.contains("Authorization:"))
    }
}
