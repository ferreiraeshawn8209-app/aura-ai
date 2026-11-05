package com.aura.voice.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.aura.voice.SpeechInputConfig
import com.aura.voice.SpeechRecognitionListener
import com.aura.voice.SpeechRecognitionProvider
import com.aura.voice.SpeechRecognitionResult
import com.aura.voice.VoiceError
import com.aura.voice.VoiceErrorCode
import com.aura.voice.VoiceOperationResult
import com.aura.voice.VoiceProviderType

class AndroidSpeechRecognizerProvider(
    context: Context,
    override val id: String = "android",
) : SpeechRecognitionProvider {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var listener: SpeechRecognitionListener? = null
    private var released = false

    override val type = VoiceProviderType.ANDROID
    override val isAvailable: Boolean
        get() = !released && SpeechRecognizer.isRecognitionAvailable(appContext)

    override fun startListening(
        config: SpeechInputConfig,
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult {
        if (!isAvailable) {
            return VoiceOperationResult.rejected(
                VoiceError(
                    VoiceErrorCode.PROVIDER_UNAVAILABLE,
                    "Android SpeechRecognizer is not available on this device.",
                ),
            )
        }
        this.listener = listener
        mainHandler.post {
            if (released) return@post
            recognizer?.cancel()
            recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).also {
                it.setRecognitionListener(recognitionListener)
                it.startListening(
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.languageTag)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, config.partialResults)
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, config.preferOffline)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    },
                )
            }
        }
        return VoiceOperationResult.accepted()
    }

    override fun stopListening() {
        mainHandler.post { recognizer?.cancel() }
    }

    override fun close() {
        if (released) return
        released = true
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
            listener = null
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onPartialResults(results: Bundle?) {
            emitResult(results, isFinal = false)
        }

        override fun onResults(results: Bundle?) {
            emitResult(results, isFinal = true)
        }

        override fun onError(errorCode: Int) {
            listener?.onResult(
                SpeechRecognitionResult(
                    transcript = "",
                    confidence = null,
                    languageTag = null,
                    isFinal = true,
                    error = VoiceError(
                        mapError(errorCode),
                        "Android SpeechRecognizer error code $errorCode.",
                        recoverable = errorCode != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                    ),
                ),
            )
        }
    }

    private fun emitResult(results: Bundle?, isFinal: Boolean) {
        val transcripts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val confidence = results
            ?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            ?.firstOrNull()
            ?.takeUnless { it < 0f }
        listener?.onResult(
            SpeechRecognitionResult(
                transcript = transcripts?.firstOrNull().orEmpty(),
                confidence = confidence,
                languageTag = null,
                isFinal = isFinal,
            ),
        )
    }

    private fun mapError(code: Int): VoiceErrorCode = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> VoiceErrorCode.AUDIO_UNAVAILABLE
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceErrorCode.AUDIO_PERMISSION_DENIED
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        -> VoiceErrorCode.NETWORK
        SpeechRecognizer.ERROR_NO_MATCH -> VoiceErrorCode.NO_MATCH
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceErrorCode.BUSY
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
        -> VoiceErrorCode.UNSUPPORTED
        else -> VoiceErrorCode.UNKNOWN
    }
}