package com.aura.ai.agent.tool

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsAppDraftToolTest {
    @Test
    fun normalizesAnInternationalNumber() {
        assertEquals("27123456789", WhatsAppDraftTool.normalizePhone("+27 (12) 345-6789"))
    }

    @Test
    fun rejectsShortOrNonnumericPhoneValues() {
        assertNull(WhatsAppDraftTool.normalizePhone("12345"))
        assertNull(WhatsAppDraftTool.normalizePhone("call Shawn"))
    }

    @Test
    fun buildsEncodedDraftLinkWithoutSendingMessage() {
        val link = WhatsAppDraftTool.buildUri("27123456789", "Hi, how are you?")
        assertTrue(link.startsWith("https://wa.me/27123456789?text="))
        val text = URLDecoder.decode(link.substringAfter("?text="), StandardCharsets.UTF_8.name())
        assertEquals("Hi, how are you?", text)
    }
}
