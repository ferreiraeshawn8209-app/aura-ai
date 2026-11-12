package com.aura.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpeechLanguageFallbackPolicyTest {
    @Test
    fun prefersBritishEnglishAfterSouthAfricanEnglishIsRejected() {
        assertEquals("en-GB", SpeechLanguageFallbackPolicy.nextLanguage("en-ZA"))
    }

    @Test
    fun triesAmericanEnglishAfterBritishEnglishIsRejected() {
        assertEquals("en-US", SpeechLanguageFallbackPolicy.nextLanguage("en-GB"))
    }

    @Test
    fun doesNotReplaceUnrelatedOrAlreadyExhaustedLanguages() {
        assertNull(SpeechLanguageFallbackPolicy.nextLanguage("en-US"))
        assertNull(SpeechLanguageFallbackPolicy.nextLanguage("de-DE"))
    }
}
