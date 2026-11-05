package com.aura.voice

import java.io.Closeable

interface SpeechRecognitionListener {
    fun onResult(result: SpeechRecognitionResult)
}

interface SpeechRecognitionProvider : Closeable {
    val id: String
    val type: VoiceProviderType
    val isAvailable: Boolean

    fun startListening(
        config: SpeechInputConfig,
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult

    fun stopListening()
}

interface SpeechSynthesisListener {
    fun onStarted()
    fun onCompleted()
    fun onStopped()
    fun onError(error: VoiceError)
}

interface TextToSpeechProvider : Closeable {
    val id: String
    val type: VoiceProviderType
    val isAvailable: Boolean

    fun getVoices(callback: (List<VoiceDescriptor>) -> Unit)

    fun speak(
        text: String,
        settings: VoiceSettings,
        listener: SpeechSynthesisListener,
    ): VoiceOperationResult

    fun stopSpeaking()
}

interface VoiceSettingsStore {
    fun load(): VoiceSettings
    fun save(settings: VoiceSettings)
}

class InMemoryVoiceSettingsStore(
    initial: VoiceSettings = VoiceSettings(),
) : VoiceSettingsStore {
    private var settings = initial.normalized()

    @Synchronized
    override fun load(): VoiceSettings = settings

    @Synchronized
    override fun save(settings: VoiceSettings) {
        this.settings = settings.normalized()
    }
}

interface ConversationStateObserver {
    fun onStateChanged(snapshot: ConversationStateSnapshot)
}

/**
 * The only API surface an Android application needs for the subsystem.
 * Callbacks are used instead of imposing a coroutine or networking library on
 * the host application.
 */
interface AuraVoiceSubsystem : Closeable {
    fun startListening(
        config: SpeechInputConfig = SpeechInputConfig(),
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult

    fun stopListening(): VoiceOperationResult

    fun speak(
        text: String,
        listener: SpeechSynthesisListener,
    ): VoiceOperationResult

    fun stopSpeaking(): VoiceOperationResult

    fun getVoices(callback: (List<VoiceDescriptor>) -> Unit)

    fun selectVoice(
        voiceId: String,
        callback: (VoiceOperationResult) -> Unit = {},
    )

    fun conversationState(): ConversationStateSnapshot

    fun observeState(observer: ConversationStateObserver): Closeable

    fun voiceSettings(): VoiceSettings

    /**
     * Persists provider, voice, language, speaking rate, and pitch settings.
     * This changes voice output only; it does not touch Aura's reasoning model.
     */
    fun updateVoiceSettings(settings: VoiceSettings): VoiceOperationResult

    /**
     * The reasoning layer calls this after a final transcript is handed off.
     * It keeps PROCESSING visible without making the voice module own AI work.
     */
    fun markProcessing(): VoiceOperationResult

    /**
     * Stops current audio/listening and emits INTERRUPTED before returning to
     * IDLE. Starting a new listen operation while speaking also interrupts.
     */
    fun interrupt(): VoiceOperationResult
}