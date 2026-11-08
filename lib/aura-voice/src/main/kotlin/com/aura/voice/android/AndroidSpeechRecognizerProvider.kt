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
    private val requestGate = RecognitionRequestGate()
    private var recognizer: SpeechRecognizer? = null
    private var listener: SpeechRecognitionListener? = null
    private var timeoutRunnable: Runnable? = null
    private var timeoutRequestId: Long? = null
    @Volatile private var released = false

    override val type = VoiceProviderType.ANDROID
    override val isAvailable: Boolean
        get() = !released && SpeechRecognizer.isRecognitionAvailable(appContext)

    override fun startListening(config: SpeechInputConfig, listener: SpeechRecognitionListener): VoiceOperationResult {
        if (!isAvailable) return VoiceOperationResult.rejected(
            VoiceError(VoiceErrorCode.PROVIDER_UNAVAILABLE, "Android SpeechRecognizer is not available on this device."),
        )

        val requestId = requestGate.begin()
        mainHandler.post {
            if (!isCurrent(requestId)) return@post
            cancelTimeout()
            destroyRecognizer()
            this.listener = listener
            if (!isAvailable) {
                deliverFailure(requestId, VoiceErrorCode.PROVIDER_UNAVAILABLE, "Android SpeechRecognizer became unavailable.")
                return@post
            }

            val next = try {
                SpeechRecognizer.createSpeechRecognizer(appContext)
            } catch (_: SecurityException) {
                deliverFailure(requestId, VoiceErrorCode.AUDIO_PERMISSION_DENIED, "Microphone permission is required.")
                return@post
            } catch (_: RuntimeException) {
                deliverFailure(requestId, VoiceErrorCode.AUDIO_UNAVAILABLE, "Could not create Android speech recognition.")
                return@post
            }

            recognizer = next
            try {
                next.setRecognitionListener(recognitionListener(requestId))
                next.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, config.languageTag)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, config.partialResults)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, config.preferOffline)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, RecognitionDiagnosticPolicy.MAX_ALTERNATIVES + 1)
                })
                scheduleTimeout(requestId)
            } catch (_: SecurityException) {
                deliverFailure(requestId, VoiceErrorCode.AUDIO_PERMISSION_DENIED, "Microphone permission is required.")
            } catch (_: RuntimeException) {
                deliverFailure(requestId, VoiceErrorCode.AUDIO_UNAVAILABLE, "Could not start Android speech recognition.")
            }
        }
        return VoiceOperationResult.accepted()
    }

    override fun stopListening() {
        requestGate.invalidate()
        mainHandler.post {
            cancelTimeout()
            listener = null
            destroyRecognizer()
        }
    }

    override fun close() {
        if (released) return
        released = true
        requestGate.invalidate()
        mainHandler.post {
            cancelTimeout()
            listener = null
            destroyRecognizer()
        }
    }

    private fun recognitionListener(requestId: Long) = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = refreshTimeout(requestId)
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
        override fun onPartialResults(results: Bundle?) {
            refreshTimeout(requestId)
            emitResult(results, false, requestId)
        }
        override fun onResults(results: Bundle?) = emitResult(results, true, requestId)
        override fun onError(errorCode: Int) = emitFailure(
            requestId,
            VoiceError(
                mapError(errorCode),
                RecognitionDiagnosticPolicy.errorMessage(errorCode),
                recoverable = errorCode != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
            ),
        )
    }

    private fun emitResult(results: Bundle?, isFinal: Boolean, requestId: Long) {
        if (!isCurrent(requestId)) return
        val transcripts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val transcript = transcripts?.firstOrNull()?.trim().orEmpty()
        val confidence = RecognitionDiagnosticPolicy.confidence(
            results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)?.firstOrNull(),
        )
        emit(requestId, SpeechRecognitionResult(
            transcript = transcript,
            confidence = confidence,
            languageTag = null, // Android does not guarantee detected-language metadata.
            isFinal = isFinal,
            alternatives = RecognitionDiagnosticPolicy.alternatives(transcripts, transcript),
        ), finish = isFinal)
    }

    private fun deliverFailure(requestId: Long, code: VoiceErrorCode, message: String) =
        emitFailure(requestId, VoiceError(code, message, recoverable = code != VoiceErrorCode.AUDIO_PERMISSION_DENIED))

    private fun emitFailure(requestId: Long, error: VoiceError) = emit(
        requestId,
        SpeechRecognitionResult("", null, null, isFinal = true, error = error),
        finish = true,
    )

    private fun emit(requestId: Long, result: SpeechRecognitionResult, finish: Boolean) {
        if (!isCurrent(requestId)) return
        if (finish) cancelTimeout(requestId)
        listener?.onResult(result)
        if (finish) mainHandler.post {
            if (isCurrent(requestId)) {
                listener = null
                destroyRecognizer()
            }
        }
    }

    private fun refreshTimeout(requestId: Long) {
        if (isCurrent(requestId)) scheduleTimeout(requestId)
    }

    private fun scheduleTimeout(requestId: Long) {
        cancelTimeout()
        val timeout = Runnable {
            if (isCurrent(requestId) && timeoutRequestId == requestId) {
                timeoutRunnable = null
                timeoutRequestId = null
                emitFailure(requestId, VoiceError(
                    VoiceErrorCode.NETWORK,
                    "Speech recognition stopped responding. Try speaking again.",
                    recoverable = true,
                ))
            }
        }
        timeoutRequestId = requestId
        timeoutRunnable = timeout
        mainHandler.postDelayed(timeout, RecognitionTimeoutPolicy.DEFAULT_TIMEOUT_MILLIS)
    }

    private fun cancelTimeout(requestId: Long? = null) {
        if (requestId != null && timeoutRequestId != requestId) return
        timeoutRunnable?.let(mainHandler::removeCallbacks)
        timeoutRunnable = null
        timeoutRequestId = null
    }

    private fun isCurrent(requestId: Long): Boolean = !released && requestGate.isCurrent(requestId)

    private fun destroyRecognizer() {
        val current = recognizer ?: return
        recognizer = null
        runCatching { current.cancel() }
        runCatching { current.destroy() }
    }

    private fun mapError(code: Int): VoiceErrorCode = when (code) {
        SpeechRecognizer.ERROR_AUDIO -> VoiceErrorCode.AUDIO_UNAVAILABLE
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceErrorCode.AUDIO_PERMISSION_DENIED
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceErrorCode.NETWORK
        SpeechRecognizer.ERROR_NO_MATCH -> VoiceErrorCode.NO_MATCH
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceErrorCode.BUSY
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> VoiceErrorCode.UNSUPPORTED
        else -> VoiceErrorCode.UNKNOWN
    }
}
