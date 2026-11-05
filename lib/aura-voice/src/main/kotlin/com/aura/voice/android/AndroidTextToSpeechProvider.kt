package com.aura.voice.android

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aura.voice.SpeechSynthesisListener
import com.aura.voice.TextToSpeechProvider
import com.aura.voice.VoiceDescriptor
import com.aura.voice.VoiceError
import com.aura.voice.VoiceErrorCode
import com.aura.voice.VoiceOperationResult
import com.aura.voice.VoiceProviderType
import com.aura.voice.VoiceSettings
import java.util.Locale

class AndroidTextToSpeechProvider(
    context: Context,
    override val id: String = "android",
) : TextToSpeechProvider {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val tts: TextToSpeech
    private var ready = false
    private var released = false
    private val listenersByUtterance = mutableMapOf<String, SpeechSynthesisListener>()
    private var utteranceCounter = 0L
    private val utterancePrefix = "aura-voice-"

    override val type = VoiceProviderType.ANDROID
    override val isAvailable: Boolean
        get() = ready && !released

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
        }
        // Post installation until every property, including progressListener,
        // has completed initialization.
        mainHandler.post { tts.setOnUtteranceProgressListener(progressListener) }
    }

    override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) {
        mainHandler.post {
            val voices = if (isAvailable) {
                tts.voices.orEmpty().map { voice ->
                    VoiceDescriptor(
                        id = voice.name,
                        name = voice.name,
                        provider = VoiceProviderType.ANDROID,
                        languageTag = voice.locale.toLanguageTag(),
                        providerId = id,
                        isNetworkRequired = voice.isNetworkConnectionRequired,
                        supportsPitch = true,
                        metadata = mapOf(
                            "quality" to voice.quality.toString(),
                            "latency" to voice.latency.toString(),
                            "gender" to voice.name.lowercase().let { name ->
                                when {
                                    "female" in name || "woman" in name -> "Female"
                                    "male" in name || "man" in name -> "Male"
                                    else -> "Voice"
                                }
                            },
                        ),
                    )
                }
            } else {
                emptyList()
            }
            callback(voices)
        }
    }

    override fun speak(
        text: String,
        settings: VoiceSettings,
        listener: SpeechSynthesisListener,
    ): VoiceOperationResult {
        if (!isAvailable) {
            return VoiceOperationResult.rejected(
                VoiceError(
                    VoiceErrorCode.PROVIDER_UNAVAILABLE,
                    "Android TextToSpeech is not initialized.",
                ),
            )
        }
        if (text.isBlank()) {
            return VoiceOperationResult.rejected(
                VoiceError(VoiceErrorCode.INVALID_CONFIGURATION, "Text to speak cannot be blank."),
            )
        }

        val locale = Locale.forLanguageTag(settings.languageTag)
        val localeStatus = tts.setLanguage(locale)
        if (localeStatus == TextToSpeech.LANG_MISSING_DATA ||
            localeStatus == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            return VoiceOperationResult.rejected(
                VoiceError(
                    VoiceErrorCode.UNSUPPORTED,
                    "Android TTS does not support language ${settings.languageTag}.",
                ),
            )
        }

        settings.voiceId?.let { voiceId ->
            val voice = tts.voices.orEmpty().firstOrNull { it.name == voiceId }
                ?: return VoiceOperationResult.rejected(
                    VoiceError(
                        VoiceErrorCode.INVALID_CONFIGURATION,
                        "Android TTS voice '$voiceId' is not installed.",
                    ),
                )
            tts.voice = voice
        }

        tts.setSpeechRate(settings.speakingRate.coerceIn(0.1f, 4.0f))
        tts.setPitch(settings.pitch.coerceIn(0.1f, 4.0f))
        val utteranceId = "$utterancePrefix${++utteranceCounter}"
        listenersByUtterance[utteranceId] = listener
        val status = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (status != TextToSpeech.SUCCESS) {
            listenersByUtterance.remove(utteranceId)
            return VoiceOperationResult.rejected(
                VoiceError(VoiceErrorCode.UNKNOWN, "Android TTS rejected the utterance."),
            )
        }
        return VoiceOperationResult.accepted()
    }

    override fun stopSpeaking() {
        if (isAvailable) {
            tts.stop()
        }
    }

    override fun close() {
        if (released) return
        released = true
        listenersByUtterance.clear()
        tts.stop()
        tts.shutdown()
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            utteranceId?.let { listenersByUtterance[it]?.onStarted() }
        }

        override fun onDone(utteranceId: String?) {
            utteranceId?.let { listenersByUtterance.remove(it)?.onCompleted() }
        }

        override fun onError(utteranceId: String?) {
            utteranceId?.let {
                listenersByUtterance.remove(it)?.onError(
                    VoiceError(VoiceErrorCode.UNKNOWN, "Android TTS failed to synthesize speech."),
                )
            }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            utteranceId?.let { listenersByUtterance.remove(it)?.onStopped() }
        }
    }
}
