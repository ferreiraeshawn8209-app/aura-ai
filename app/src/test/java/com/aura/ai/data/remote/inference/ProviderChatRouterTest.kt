package com.aura.ai.data.remote.inference

import com.aura.ai.data.remote.ollama.OllamaChatMessage
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderChatRouterTest {
    private val localCall = ChatCompletionCall(
        model = "qwen3:1.7b",
        messages = listOf(OllamaChatMessage("user", "hello")),
        temperature = 0.2,
        timeoutSeconds = 30,
    )
    private val cloudCall = localCall.copy(
        model = "provider/model",
        baseUrl = "https://example.test/v1",
        apiKey = "test-secret",
    )

    @Test
    fun usesCloudFirstAndDoesNotCallLocalOnSuccess() = runBlocking {
        val cloud = FakeBackend("cloud answer")
        val local = FakeBackend("local answer")
        val result = ProviderChatRouter(cloud, local).complete(localCall, cloudCall)

        assertEquals("cloud answer", result.text)
        assertEquals("cloud", result.provider)
        assertEquals(1, cloud.calls)
        assertEquals(0, local.calls)
    }

    @Test
    fun cloudFailureFallsBackAndCooldownThenRecoversToCloud() = runBlocking {
        var now = 100L
        val cloud = FakeBackend(failure = IOException("unavailable"))
        val local = FakeBackend("local answer")
        val router = ProviderChatRouter(cloud, local, CloudCircuitBreaker(50) { now })

        val failedOver = router.complete(localCall, cloudCall)
        assertEquals("local", failedOver.provider)
        assertNotNull(failedOver.fallbackReason)
        assertEquals(1, cloud.calls)
        assertEquals(1, local.calls)

        val duringCooldown = router.complete(localCall, cloudCall)
        assertEquals("local", duringCooldown.provider)
        assertTrue(duringCooldown.fallbackReason!!.contains("cooling down"))
        assertEquals(1, cloud.calls)

        now += 51
        cloud.failure = null
        cloud.response = "cloud recovered"
        val recovered = router.complete(localCall, cloudCall)
        assertEquals("cloud recovered", recovered.text)
        assertEquals("cloud", recovered.provider)
        assertEquals(2, cloud.calls)
        assertEquals(2, local.calls)
    }

    @Test
    fun incompleteCloudConfigurationUsesLocal() = runBlocking {
        val cloud = FakeBackend("unused")
        val local = FakeBackend("local answer")
        val result = ProviderChatRouter(cloud, local).complete(
            localCall,
            cloudCall.copy(apiKey = null),
        )

        assertEquals("local", result.provider)
        assertEquals(0, cloud.calls)
        assertEquals(1, local.calls)
    }

    @Test
    fun callerCancellationIsNotConvertedIntoFallback() = runBlocking {
        val cloud = FakeBackend(failure = CancellationException("caller stopped"))
        val local = FakeBackend("must not run")
        val result = runCatching {
            ProviderChatRouter(cloud, local).complete(localCall, cloudCall)
        }

        assertTrue(result.exceptionOrNull() is CancellationException)
        assertFalse(local.calls > 0)
    }

    private class FakeBackend(
        var response: String = "",
        var failure: Throwable? = null,
    ) : ChatCompletionBackend {
        var calls: Int = 0

        override suspend fun complete(call: ChatCompletionCall): String {
            calls++
            failure?.let { throw it }
            return response
        }
    }
}
