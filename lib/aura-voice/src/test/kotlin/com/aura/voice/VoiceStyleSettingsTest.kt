package com.aura.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceStyleSettingsTest {
    @Test
    fun clampsProviderStyleRangeAndRecoversInvalidValues() {
        assertEquals(0.0f, VoiceSettings(styleExaggeration = -1.0f).normalized().styleExaggeration, 0.0f)
        assertEquals(1.0f, VoiceSettings(styleExaggeration = 2.0f).normalized().styleExaggeration, 0.0f)
        assertEquals(0.15f, VoiceSettings(styleExaggeration = Float.NaN).normalized().styleExaggeration, 0.0f)
        assertEquals(0.15f, VoiceSettings(styleExaggeration = Float.POSITIVE_INFINITY).normalized().styleExaggeration, 0.0f)
    }

    @Test
    fun preservesVoiceIdentityAndSelectedStyle() {
        val settings = VoiceSettings(provider = VoiceProviderType.CLOUD, providerId = "elevenlabs", voiceId = "elevenlabs:chosen", styleExaggeration = 0.7f).normalized()
        assertEquals("elevenlabs:chosen", settings.voiceId)
        assertEquals(0.7f, settings.styleExaggeration, 0.0f)
        assertEquals(0.15f, VoiceSettings().styleExaggeration, 0.0f)
    }
}

