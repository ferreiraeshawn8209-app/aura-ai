package com.aura.voice

import java.io.Closeable

/**
 * The app supplies a transport implementation. The transport should call a
 * server-side provider or a provider SDK; API keys must not be embedded in the
 * Android application.
 */
interface CloudSpeechTransport : Closeable {
    val isAvailable: Boolean

    fun start(
        config: SpeechInputConfig,
        listener: SpeechRecognitionListener,
    ): Closeable
}

class CloudSpeechRecognitionProvider(
    override val id: String,
    private val transport: CloudSpeechTransport,
) : SpeechRecognitionProvider {
    override val type = VoiceProviderType.CLOUD
    override val isAvailable: Boolean
        get() = transport.isAvailable
    private var session: Closeable? = null

    @Synchronized
    override fun startListening(
        config: SpeechInputConfig,
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult {
        if (!isAvailable) {
            return VoiceOperationResult.rejected(
                VoiceError(
                    VoiceErrorCode.PROVIDER_NOT_CONFIGURED,
                    "Cloud speech transport is unavailable or not configured.",
                ),
            )
        }
        session?.close()
        session = transport.start(config, listener)
        return VoiceOperationResult.accepted()
    }

    @Synchronized
    override fun stopListening() {
        session?.close()
        session = null
    }

    override fun close() {
        stopListening()
        transport.close()
    }
}

data class CloudSynthesisRequest(
    val text: String,
    val settings: VoiceSettings,
)

interface CloudTextToSpeechTransport : Closeable {
    val isAvailable: Boolean

    fun getVoices(callback: (List<VoiceDescriptor>) -> Unit)

    fun synthesize(
        request: CloudSynthesisRequest,
        callback: (Result<ByteArray>) -> Unit,
    )
}

interface CloudAudioPlayer {
    fun play(
        audio: ByteArray,
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onStopped: () -> Unit,
        onError: (VoiceError) -> Unit,
    ): Closeable

    fun stop()
}

class CloudTextToSpeechProvider(
    override val id: String,
    private val transport: CloudTextToSpeechTransport,
    private val audioPlayer: CloudAudioPlayer,
) : TextToSpeechProvider {
    override val type = VoiceProviderType.CLOUD
    override val isAvailable: Boolean
        get() = transport.isAvailable
    private var playback: Closeable? = null
    private var synthesisGeneration = 0L

    override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) {
        if (isAvailable) transport.getVoices(callback) else callback(emptyList())
    }

    @Synchronized
    override fun speak(
        text: String,
        settings: VoiceSettings,
        listener: SpeechSynthesisListener,
    ): VoiceOperationResult {
        if (!isAvailable) {
            return VoiceOperationResult.rejected(
                VoiceError(
                    VoiceErrorCode.PROVIDER_NOT_CONFIGURED,
                    "Cloud TTS transport is unavailable or not configured.",
                ),
            )
        }
        if (text.isBlank()) {
            return VoiceOperationResult.rejected(
                VoiceError(VoiceErrorCode.INVALID_CONFIGURATION, "Text to speak cannot be blank."),
            )
        }

        playback?.close()
        val generation = ++synthesisGeneration
        transport.synthesize(CloudSynthesisRequest(text, settings), { result ->
            synchronized(this) {
                if (generation != synthesisGeneration) return@synchronized
                result.fold(
                    onSuccess = { audio ->
                        playback = audioPlayer.play(
                            audio,
                            listener::onStarted,
                            listener::onCompleted,
                            listener::onStopped,
                            listener::onError,
                        )
                    },
                    onFailure = {
                        listener.onError(
                            VoiceError(
                                VoiceErrorCode.NETWORK,
                                "Cloud TTS synthesis failed.",
                            ),
                        )
                    },
                )
            }
        })
        return VoiceOperationResult.accepted()
    }

    @Synchronized
    override fun stopSpeaking() {
        synthesisGeneration++
        playback?.close()
        playback = null
        audioPlayer.stop()
    }

    override fun close() {
        stopSpeaking()
        transport.close()
    }
}