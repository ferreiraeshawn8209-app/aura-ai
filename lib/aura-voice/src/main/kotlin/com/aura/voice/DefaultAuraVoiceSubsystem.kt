package com.aura.voice

import java.io.Closeable

class DefaultAuraVoiceSubsystem(
    speechProviders: List<SpeechRecognitionProvider>,
    textToSpeechProviders: List<TextToSpeechProvider>,
    private val settingsStore: VoiceSettingsStore = InMemoryVoiceSettingsStore(),
) : AuraVoiceSubsystem {
    private val speechProvidersById = speechProviders.associateBy { it.id }
    private val ttsProvidersById = textToSpeechProviders.associateBy { it.id }
    private val observers = LinkedHashSet<ConversationStateObserver>()
    private var settings = settingsStore.load().normalized()
    private var snapshot = ConversationStateSnapshot(ConversationState.IDLE)
    private var speechGeneration = 0L
    private var synthesisGeneration = 0L
    private var recognitionActive = false
    private var synthesisActive = false
    private var processingActive = false
    private var closed = false

    override fun startListening(
        config: SpeechInputConfig,
        listener: SpeechRecognitionListener,
    ): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)

        val provider = speechProvider()
            ?: return rejected(
                VoiceErrorCode.PROVIDER_UNAVAILABLE,
                "No available speech recognition provider is selected.",
            )

        val monitorCurrentSpeech = config.monitorSpeechOutput && synthesisActive
        if (synthesisActive && !monitorCurrentSpeech) {
            interruptLocked()
        }

        if (!monitorCurrentSpeech) synthesisGeneration++
        processingActive = false
        recognitionActive = true
        val generation = ++speechGeneration
        transitionForActivityLocked()
        val result = provider.startListening(
            config.copy(languageTag = config.languageTag.ifBlank { settings.languageTag }),
            object : SpeechRecognitionListener {
                override fun onResult(result: SpeechRecognitionResult) {
                    synchronized(this@DefaultAuraVoiceSubsystem) {
                        if (closed || generation != speechGeneration) return
                        if (result.error != null) {
                            recognitionActive = false
                            processingActive = false
                            transitionForActivityLocked(result.error)
                        } else if (result.isFinal) {
                            recognitionActive = false
                            processingActive = true
                            transitionForActivityLocked()
                        } else {
                            transitionForActivityLocked()
                        }
                        listener.onResult(result)
                    }
                }
            },
        )
        if (!result.accepted) {
            recognitionActive = false
            processingActive = false
            transitionForActivityLocked(result.error)
        }
        result
    }

    override fun stopListening(): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
        speechGeneration++
        speechProvider()?.stopListening()
        recognitionActive = false
        processingActive = false
        transitionForActivityLocked()
        VoiceOperationResult.accepted()
    }

    override fun speak(
        text: String,
        listener: SpeechSynthesisListener,
    ): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
        if (text.isBlank()) {
            return rejected(VoiceErrorCode.INVALID_CONFIGURATION, "Text to speak cannot be blank.")
        }

        val provider = ttsProvider()
            ?: return rejected(
                VoiceErrorCode.PROVIDER_UNAVAILABLE,
                "No available text-to-speech provider matches the selected voice settings.",
            )

        speechGeneration++
        speechProvider()?.stopListening()
        recognitionActive = false
        processingActive = false
        synthesisGeneration++
        synthesisActive = true
        val generation = synthesisGeneration
        transitionLocked(ConversationState.SPEAKING)
        val result = provider.speak(
            text,
            settings,
            object : SpeechSynthesisListener {
                override fun onStarted() {
                    synchronized(this@DefaultAuraVoiceSubsystem) {
                        if (!closed && generation == synthesisGeneration) listener.onStarted()
                    }
                }

                override fun onCompleted() {
                    synchronized(this@DefaultAuraVoiceSubsystem) {
                        if (closed || generation != synthesisGeneration) return
                        synthesisActive = false
                        transitionForActivityLocked()
                        listener.onCompleted()
                    }
                }

                override fun onStopped() {
                    synchronized(this@DefaultAuraVoiceSubsystem) {
                        if (closed || generation != synthesisGeneration) return
                        synthesisActive = false
                        transitionLocked(ConversationState.INTERRUPTED)
                        transitionForActivityLocked()
                        listener.onStopped()
                    }
                }

                override fun onError(error: VoiceError) {
                    synchronized(this@DefaultAuraVoiceSubsystem) {
                        if (closed || generation != synthesisGeneration) return
                        synthesisActive = false
                        transitionForActivityLocked(error)
                        listener.onError(error)
                    }
                }
            },
        )
        if (!result.accepted) {
            synthesisActive = false
            transitionForActivityLocked(result.error)
        }
        result
    }

    override fun stopSpeaking(): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
        if (!synthesisActive) {
            return VoiceOperationResult.accepted()
        }
        synthesisGeneration++
        ttsProvider()?.stopSpeaking()
        synthesisActive = false
        transitionLocked(ConversationState.INTERRUPTED)
        transitionForActivityLocked()
        VoiceOperationResult.accepted()
    }

    override fun getVoices(callback: (List<VoiceDescriptor>) -> Unit) {
        val providers = synchronized(this) {
            ttsProvidersById.values.filter { it.isAvailable }
        }
        if (providers.isEmpty()) {
            callback(emptyList())
            return
        }

        val lock = Any()
        val allVoices = mutableListOf<VoiceDescriptor>()
        var remaining = providers.size
        providers.forEach { provider ->
            provider.getVoices { voices ->
                synchronized(lock) {
                    allVoices += voices.map { voice ->
                        if (voice.providerId == null) {
                            voice.copy(providerId = provider.id)
                        } else {
                            voice
                        }
                    }
                    remaining -= 1
                    if (remaining == 0) callback(allVoices.toList())
                }
            }
        }
    }

    override fun selectVoice(
        voiceId: String,
        callback: (VoiceOperationResult) -> Unit,
    ) {
        if (voiceId.isBlank()) {
            callback(
                VoiceOperationResult.rejected(
                    VoiceError(VoiceErrorCode.INVALID_CONFIGURATION, "Voice id cannot be blank."),
                ),
            )
            return
        }

        getVoices { voices ->
            synchronized(this) {
                if (closed) {
                    callback(rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false))
                    return@synchronized
                }
                val voice = voices.firstOrNull { it.id == voiceId }
                if (voice == null) {
                    callback(
                        rejected(
                            VoiceErrorCode.INVALID_CONFIGURATION,
                            "Voice '$voiceId' is not available from the selected provider.",
                        ),
                    )
                    return@synchronized
                }
                settings = settings.copy(
                    provider = voice.provider,
                    providerId = voice.providerId,
                    voiceId = voice.id,
                    languageTag = voice.languageTag,
                ).normalized()
                settingsStore.save(settings)
                callback(VoiceOperationResult.accepted())
            }
        }
    }

    override fun conversationState(): ConversationStateSnapshot = synchronized(this) { snapshot }

    override fun observeState(observer: ConversationStateObserver): Closeable {
        synchronized(this) {
            if (!closed) {
                observers += observer
                observer.onStateChanged(snapshot)
            }
        }
        return Closeable { synchronized(this@DefaultAuraVoiceSubsystem) { observers -= observer } }
    }

    override fun voiceSettings(): VoiceSettings = synchronized(this) { settings }

    override fun updateVoiceSettings(newSettings: VoiceSettings): VoiceOperationResult =
        synchronized(this) {
            if (closed) {
                return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
            }
            settings = newSettings.normalized()
            settingsStore.save(settings)
            VoiceOperationResult.accepted()
        }

    override fun markProcessing(): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
        processingActive = true
        transitionLocked(ConversationState.PROCESSING)
        VoiceOperationResult.accepted()
    }

    override fun interrupt(): VoiceOperationResult = synchronized(this) {
        if (closed) return rejected(VoiceErrorCode.UNKNOWN, "Voice subsystem is closed", false)
        interruptLocked()
        VoiceOperationResult.accepted()
    }

    override fun close() {
        synchronized(this) {
            if (closed) return
            closed = true
            speechGeneration++
            synthesisGeneration++
            recognitionActive = false
            synthesisActive = false
            processingActive = false
            speechProvidersById.values.forEach { it.close() }
            ttsProvidersById.values.forEach { it.close() }
            observers.clear()
        }
    }

    private fun speechProvider(): SpeechRecognitionProvider? {
        val configured = settings.providerId?.let(speechProvidersById::get)
        return configured?.takeIf { it.type == settings.provider && it.isAvailable }
            ?: speechProvidersById.values.firstOrNull {
                it.type == settings.provider && it.isAvailable
            }
            ?: speechProvidersById.values.firstOrNull { it.isAvailable }
    }

    private fun ttsProvider(): TextToSpeechProvider? {
        val selectedProviderId = settings.providerId
        if (selectedProviderId != null) {
            return ttsProvidersById[selectedProviderId]
                ?.takeIf { it.type == settings.provider && it.isAvailable }
        }
        return ttsProvidersById.values.firstOrNull {
            it.type == settings.provider && it.isAvailable
        }
    }

    private fun interruptLocked() {
        speechGeneration++
        synthesisGeneration++
        speechProvider()?.stopListening()
        ttsProvider()?.stopSpeaking()
        recognitionActive = false
        synthesisActive = false
        processingActive = false
        transitionLocked(ConversationState.INTERRUPTED)
        transitionLocked(ConversationState.IDLE)
    }

    private fun transitionForActivityLocked(error: VoiceError? = null) {
        val state = when {
            recognitionActive && synthesisActive -> ConversationState.LISTENING_AND_SPEAKING
            synthesisActive -> ConversationState.SPEAKING
            recognitionActive -> ConversationState.LISTENING
            processingActive -> ConversationState.PROCESSING
            error != null -> ConversationState.ERROR
            else -> ConversationState.IDLE
        }
        transitionLocked(state, error)
    }

    private fun transitionLocked(
        state: ConversationState,
        error: VoiceError? = null,
    ) {
        snapshot = ConversationStateSnapshot(state, error)
        observers.toList().forEach { it.onStateChanged(snapshot) }
    }

    private fun rejected(
        code: VoiceErrorCode,
        message: String,
        recoverable: Boolean = true,
    ) = VoiceOperationResult.rejected(VoiceError(code, message, recoverable))
}
