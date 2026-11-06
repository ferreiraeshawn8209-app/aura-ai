package com.aura.ai.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatBackendAvailabilityPolicyTest {
    @Test
    fun cloudOnlyConfigurationEnablesChatWithoutLocalModel() {
        val result = ChatBackendAvailabilityPolicy.evaluate(
            localModel = null,
            cloudEnabled = true,
            cloudBaseUrl = "https://openrouter.ai/api/v1",
            cloudModel = "provider/model",
            cloudApiKeyConfigured = true,
        )

        assertTrue(result.ready)
        assertEquals("Cloud · provider/model", result.label)
        assertEquals(null, result.banner)
    }

    @Test
    fun configuredCloudWithLocalModelShowsCloudFirstAndFallback() {
        val result = ChatBackendAvailabilityPolicy.evaluate(
            localModel = "qwen3:1.7b",
            cloudEnabled = true,
            cloudBaseUrl = "https://openrouter.ai/api/v1",
            cloudModel = "provider/model",
            cloudApiKeyConfigured = true,
        )

        assertTrue(result.ready)
        assertEquals("Cloud first · provider/model · Ollama fallback", result.label)
    }

    @Test
    fun localOnlyConfigurationRemainsAvailable() {
        val result = ChatBackendAvailabilityPolicy.evaluate(
            localModel = "qwen3:4b-q4_K_M",
            cloudEnabled = false,
            cloudBaseUrl = "",
            cloudModel = "",
            cloudApiKeyConfigured = false,
        )

        assertTrue(result.ready)
        assertEquals("Ollama · qwen3:4b-q4_K_M", result.label)
    }

    @Test
    fun incompleteCloudOnlyConfigurationDoesNotPretendReady() {
        val result = ChatBackendAvailabilityPolicy.evaluate(
            localModel = " ",
            cloudEnabled = true,
            cloudBaseUrl = "https://openrouter.ai/api/v1",
            cloudModel = "provider/model",
            cloudApiKeyConfigured = false,
        )

        assertFalse(result.ready)
        assertEquals("no model connected", result.label)
        assertTrue(result.banner!!.contains("API key"))
    }
}
