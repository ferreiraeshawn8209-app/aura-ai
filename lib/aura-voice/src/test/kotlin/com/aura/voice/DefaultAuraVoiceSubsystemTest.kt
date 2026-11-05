package com.aura.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DefaultAuraVoiceSubsystemTest {
    @Test
    fun unavailableSelectedTtsProviderDoesNotSilentlySwitch() {
        val alternate = FakeTtsProvider(id = "alternate")
        val subsystem = DefaultAuraVoiceSubsystem(
            speechProviders = emptyList(),
            textToSpeechProviders = listOf(alternate),
            settingsStore = InMemoryVoiceSettingsStore(
                VoiceSettings(provider = VoiceProviderType.ANDROID, providerId = "selected-but-missing"),
            ),
        )

        val result = subsystem.speak("Test", NoopSynthesisListener)

        assertFalse(result.accepted)
        assertEquals(0, alternate.speakCalls)
        subsystem.close()
    }

    @Test
    fun selectedProviderIsUsedWhenAvailable() {
        val selected = FakeTtsProvider(id = "selected")
        val alternate = FakeTtsProvider(id = "alternate")
        val subsystem = DefaultAuraVoiceSubsystem(
            speechProviders = emptyList(),
            textToSpeechProviders = listOf(alternate, selected),
            settingsStore = InMemoryVoiceSettingsStore(
                VoiceSettings(provider = VoiceProviderType.ANDROID, providerId = "selected"),
            ),
        )

        val result = subsystem.speak("Test", NoopSynthesisListener)

        assertEquals(true, result.accepted)
        assertEquals(1, selected.speakCalls)
        assertEquals(0, alternate.speakCalls)
        subsystem.close()
    }

    private class FakeTtsProvider(override val id: String) : TextToSpeechProvider {
        override val type = VoiceProviderType.ANDROID
        override val isAvailable = true
        var speakCalls = 0

        override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) = callback(emptyList())

        override fun speak(text: String, settings: VoiceSettings, listener: SpeechSynthesisListener): VoiceOperationResult {
            speakCalls++
            return VoiceOperationResult.accepted()
        }

        override fun stopSpeaking() = Unit
        override fun close() = Unit
    }

    private object NoopSynthesisListener : SpeechSynthesisListener {
        override fun onStarted() = Unit
        override fun onCompleted() = Unit
        override fun onStopped() = Unit
        override fun onError(error: VoiceError) = Unit
    }
}
