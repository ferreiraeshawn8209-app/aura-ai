package com.aura.ai.agent

import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentVerdictTest {
    @Test
    fun rejectsMissingOrMalformedVerifierResults() {
        val invalid = listOf(
            "",
            "not JSON",
            "{}",
            """{"reasoning":"No verified flag"}""",
            """{"verified":"yes","reasoning":"Bad boolean"}""",
            """{"verified":true}"""
        )
        invalid.forEach { response ->
            assertFalse("Unexpected verification for $response", parseAgentVerdict(response, Json).verified)
        }
    }

    @Test
    fun acceptsOnlyExplicitValidVerifiedVerdict() {
        assertTrue(
            parseAgentVerdict("""{"verified":true,"reasoning":"Observed evidence supports the result"}""", Json).verified
        )
        assertFalse(
            parseAgentVerdict("""{"verified":false,"reasoning":"Criteria not met"}""", Json).verified
        )
    }
}
