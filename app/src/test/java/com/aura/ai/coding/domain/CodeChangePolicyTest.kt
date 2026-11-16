package com.aura.ai.coding.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeChangePolicyTest {
    private val policy = CodeChangePolicy()

    @Test
    fun allowsNormalSourceAndTestFiles() {
        assertTrue(policy.isAllowed("app/src/main/java/com/aura/ai/Feature.kt"))
        assertTrue(policy.isAllowed("app/src/test/java/com/aura/ai/FeatureTest.kt"))
    }

    @Test
    fun rejectsTraversalAndAbsolutePaths() {
        assertFalse(policy.isAllowed("../outside.txt"))
        assertFalse(policy.isAllowed("app/../../outside.txt"))
        assertFalse(policy.isAllowed("/etc/passwd"))
        assertFalse(policy.isAllowed("C:\\Windows\\system.ini"))
    }

    @Test
    fun rejectsSecretsAndSigningMaterial() {
        assertFalse(policy.isAllowed("local.properties"))
        assertFalse(policy.isAllowed(".env.production"))
        assertFalse(policy.isAllowed("keys/release.jks"))
        assertFalse(policy.isAllowed("config/private_key.pem"))
        assertFalse(policy.isAllowed("config/client_credentials.json"))
    }

    @Test
    fun rejectsBuildInternalsAndWorkflowChanges() {
        assertFalse(policy.isAllowed("app/build/generated/BuildConfig.java"))
        assertFalse(policy.isAllowed(".gradle/caches/state.bin"))
        assertFalse(policy.isAllowed(".github/workflows/release.yml"))
    }
}
