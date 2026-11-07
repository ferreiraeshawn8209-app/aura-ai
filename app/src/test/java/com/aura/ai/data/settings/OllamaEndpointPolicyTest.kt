package com.aura.ai.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class OllamaEndpointPolicyTest {
    private val pcEndpoint = "http://192.168.8.18:11434"

    @Test
    fun loopbackAndEmulatorDefaultsResolveToTheConfiguredPcEndpoint() {
        listOf(
            "http://127.0.0.1:11434",
            "http://localhost:11434/",
            "http://10.0.2.2:11434",
            "http://0.0.0.0:11434",
        ).forEach { legacy ->
            assertEquals(legacy, pcEndpoint, OllamaEndpointPolicy.normalize(legacy, pcEndpoint))
        }
    }

    @Test
    fun keepsAUserConfiguredLanEndpoint() {
        val configured = "http://192.168.8.25:11434/"

        assertEquals(configured, OllamaEndpointPolicy.normalize(configured, pcEndpoint))
    }

    @Test
    fun missingValueUsesConfiguredDefault() {
        assertEquals(pcEndpoint, OllamaEndpointPolicy.normalize(null, pcEndpoint))
    }
}