package com.aura.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun monitoringSpeechOutputKeepsTtsActiveAndPublishesCombinedState() {
        val speech = FakeSpeechProvider()
        val tts = FakeTtsProvider("selected")
        val subsystem = DefaultAuraVoiceSubsystem(
            speechProviders = listOf(speech),
            textToSpeechProviders = listOf(tts),
            settingsStore = InMemoryVoiceSettingsStore(
                VoiceSettings(provider = VoiceProviderType.ANDROID, providerId = "selected"),
            ),
        )
        assertTrue(subsystem.speak("Aura reply", NoopSynthesisListener).accepted)

        val result = subsystem.startListening(
            SpeechInputConfig(monitorSpeechOutput = true),
            object : SpeechRecognitionListener {
                override fun onResult(result: SpeechRecognitionResult) = Unit
            },
        )

        assertTrue(result.accepted)
        assertEquals(0, tts.stopCalls)
        assertEquals(ConversationState.LISTENING_AND_SPEAKING, subsystem.conversationState().state)
        tts.finish()
        assertEquals(ConversationState.LISTENING, subsystem.conversationState().state)
        subsystem.close()
    }

    @Test
    fun normalListeningStillInterruptsSpeechOutput() {
        val speech = FakeSpeechProvider()
        val tts = FakeTtsProvider("selected")
        val subsystem = DefaultAuraVoiceSubsystem(
            speechProviders = listOf(speech),
            textToSpeechProviders = listOf(tts),
            settingsStore = InMemoryVoiceSettingsStore(
                VoiceSettings(provider = VoiceProviderType.ANDROID, providerId = "selected"),
            ),
        )
        subsystem.speak("Aura reply", NoopSynthesisListener)

        val result = subsystem.startListening(listener = object : SpeechRecognitionListener {
            override fun onResult(result: SpeechRecognitionResult) = Unit
        })

        assertTrue(result.accepted)
        assertEquals(1, tts.stopCalls)
        assertEquals(ConversationState.LISTENING, subsystem.conversationState().state)
        subsystem.close()
    }

    private class FakeSpeechProvider : SpeechRecognitionProvider {
        override val id = "speech"
        override val type = VoiceProviderType.ANDROID
        override val isAvailable = true
        override fun startListening(config: SpeechInputConfig, listener: SpeechRecognitionListener) = VoiceOperationResult.accepted()
        override fun stopListening() = Unit
        override fun close() = Unit
    }

    private class FakeTtsProvider(override val id: String) : TextToSpeechProvider {
        override val type = VoiceProviderType.ANDROID
        override val isAvailable = true
        var speakCalls = 0
        var stopCalls = 0
        private var synthesisListener: SpeechSynthesisListener? = null

        override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) = callback(emptyList())

        override fun speak(text: String, settings: VoiceSettings, listener: SpeechSynthesisListener): VoiceOperationResult {
            speakCalls++
            synthesisListener = listener
            listener.onStarted()
            return VoiceOperationResult.accepted()
        }

        fun finish() { synthesisListener?.onCompleted() }

        override fun stopSpeaking() {
            stopCalls++
            synthesisListener?.onStopped()
        }
        override fun close() = Unit
    }

    private object NoopSynthesisListener : SpeechSynthesisListener {
        override fun onStarted() = Unit
        override fun onCompleted() = Unit
        override fun onStopped() = Unit
        override fun onError(error: VoiceError) = Unit
    }
}
