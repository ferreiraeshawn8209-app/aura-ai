package com.aura.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceTuningPolicyTest {
    @Test
    fun keepsRequestedNaturalPitchAndRate() {
        val tuned = VoiceTuningPolicy.apply(VoiceSettings(voiceId = "selected"), pitch = 0.8f, speakingRate = 0.95f)

        assertEquals("selected", tuned.voiceId)
        assertEquals(0.8f, tuned.pitch, 0.001f)
        assertEquals(0.95f, tuned.speakingRate, 0.001f)
    }

    @Test
    fun clampsTuningToNaturalRangeWithoutChangingVoiceIdentity() {
        val tuned = VoiceTuningPolicy.apply(
            VoiceSettings(provider = VoiceProviderType.ANDROID, voiceId = "same-voice"),
            pitch = 4.0f,
            speakingRate = 0.1f,
        )

        assertEquals("same-voice", tuned.voiceId)
        assertEquals(VoiceTuningPolicy.PITCH_MAX, tuned.pitch, 0.001f)
        assertEquals(VoiceTuningPolicy.RATE_MIN, tuned.speakingRate, 0.001f)
    }
}
