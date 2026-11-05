package com.aura.ai.voice.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class TextToSpeechManager @Inject constructor(
    private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun initialize(onInitComplete: (success: Boolean) -> Unit = {}) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) { _isSpeaking.value = true }
                    override fun onDone(utteranceId: String?) { _isSpeaking.value = false }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) { _isSpeaking.value = false }
                })
                _isInitialized.value = true
                onInitComplete(true)
            } else {
                _error.value = "TTS initialization failed"
                onInitComplete(false)
            }
        }
    }

    fun speak(text: String) {
        if (!_isInitialized.value) {
            _error.value = "TTS not initialized"
            return
        }

        if (text.isEmpty()) return

        _isSpeaking.value = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setLanguage(locale: Locale) {
        tts?.language = locale
    }

    fun getAvailableLanguages(): Set<Locale>? {
        return try {
            tts?.availableLanguages
        } catch (e: Exception) {
            null
        }
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
