package com.aura.ai.agent.tool

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AndroidActionInputPolicyTest {
    @Test
    fun acceptsAndNormalizesInternationalPhoneNumbers() {
        assertEquals("27123456789", AndroidActionInputPolicy.normalizePhone("+27 (12) 345-6789"))
    }

    @Test
    fun rejectsInvalidPhoneInputs() {
        assertNull(AndroidActionInputPolicy.normalizePhone("12345"))
        assertNull(AndroidActionInputPolicy.normalizePhone("+27; rm -rf"))
    }

    @Test
    fun addsHttpsToBareDomainAndKeepsExplicitHttps() {
        assertEquals("https://example.com", AndroidActionInputPolicy.webUrl("example.com"))
        assertEquals("https://example.com/path", AndroidActionInputPolicy.webUrl("https://example.com/path"))
    }

    @Test
    fun rejectsNonWebSchemesAndUrlsWithoutHost() {
        assertNull(AndroidActionInputPolicy.webUrl("intent://example.com"))
        assertNull(AndroidActionInputPolicy.webUrl("file:///etc/passwd"))
        assertNull(AndroidActionInputPolicy.webUrl("https:///missing-host"))
    }
}
