package com.aura.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/** One emission per recognition lifecycle event, consumed by the UI. */
sealed interface SpeechEvent {
    data object ReadyForSpeech : SpeechEvent
    data object BeginningOfSpeech : SpeechEvent
    data class Partial(val text: String) : SpeechEvent
    data class Final(val text: String) : SpeechEvent
    data class Error(val message: String) : SpeechEvent
    data object EndOfSpeech : SpeechEvent
}

/**
 * Wraps Android's SpeechRecognizer as a cold Flow.
 *
 * Offline recognition is requested when available, but is not forced. This lets
 * Android fall back to its configured recognition provider when an offline model
 * is unavailable for the device language.
 */
@Singleton
class SpeechToText @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun listen(): Flow<SpeechEvent> = callbackFlow {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            trySend(SpeechEvent.Error("Speech recognition is not available on this device."))
            close()
            return@callbackFlow
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Do not force offline recognition. Some devices/languages have no
            // installed offline model, which otherwise causes intermittent errors.
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { trySend(SpeechEvent.ReadyForSpeech) }
            override fun onBeginningOfSpeech() { trySend(SpeechEvent.BeginningOfSpeech) }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { trySend(SpeechEvent.EndOfSpeech) }

            override fun onError(error: Int) {
                trySend(SpeechEvent.Error(errorMessage(error)))
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.let { trySend(SpeechEvent.Partial(it)) }
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                trySend(SpeechEvent.Final(text))
                close()
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        recognizer.setRecognitionListener(listener)
        try {
            recognizer.startListening(intent)
        } catch (t: Throwable) {
            trySend(SpeechEvent.Error("Unable to start speech recognition: ${t.message ?: "unknown error"}"))
            close()
        }

        awaitClose {
            try {
                recognizer.stopListening()
            } catch (_: Throwable) {
            }
            recognizer.cancel()
            recognizer.destroy()
        }
    }

    private fun errorMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Try the microphone again."
        SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer error. Try the microphone again."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        SpeechRecognizer.ERROR_NETWORK -> "Speech recognition needs a network connection for this language."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition network timeout. Try again."
        SpeechRecognizer.ERROR_NO_MATCH -> "I didn't catch that. Try speaking again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Try again in a moment."
        SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error. Try again."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Try again."
        else -> "Speech recognition error ($code)."
    }
}
