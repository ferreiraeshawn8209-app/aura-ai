package com.aura.voice

import java.util.Locale

/** The lifecycle states exposed to the Android application. */
enum class ConversationState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    LISTENING_AND_SPEAKING,
    ERROR,
    INTERRUPTED,
}

enum class VoiceProviderType {
    ANDROID,
    CLOUD,
    LOCAL,
    CUSTOM,
}

enum class VoiceErrorCode {
    AUDIO_PERMISSION_DENIED,
    AUDIO_UNAVAILABLE,
    PROVIDER_UNAVAILABLE,
    PROVIDER_NOT_CONFIGURED,
    NETWORK,
    NO_MATCH,
    BUSY,
    INVALID_CONFIGURATION,
    UNSUPPORTED,
    UNKNOWN,
}

data class VoiceError(
    val code: VoiceErrorCode,
    val message: String,
    val recoverable: Boolean = true,
)

data class SpeechInputConfig(
    val languageTag: String = Locale.getDefault().toLanguageTag(),
    val partialResults: Boolean = true,
    val preferOffline: Boolean = false,
    /** Keep current TTS running while listening for a user interruption. */
    val monitorSpeechOutput: Boolean = false,
)

data class SpeechRecognitionResult(
    val transcript: String,
    val confidence: Float?,
    val languageTag: String?,
    val isFinal: Boolean,
    val error: VoiceError? = null,
    /** Alternate hypotheses from Android, for a local diagnostic only. */
    val alternatives: List<String> = emptyList(),
)

data class VoiceDescriptor(
    val id: String,
    val name: String,
    val provider: VoiceProviderType,
    val languageTag: String,
    val providerId: String? = null,
    val isNetworkRequired: Boolean = false,
    val supportsPitch: Boolean = true,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Voice choices are deliberately independent of Aura's reasoning model.
 * There is no model identifier in this object by design.
 */
data class VoiceSettings(
    val provider: VoiceProviderType = VoiceProviderType.ANDROID,
    val providerId: String? = null,
    val voiceId: String? = null,
    val languageTag: String = Locale.getDefault().toLanguageTag(),
    val speakingRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val styleExaggeration: Float = 0.15f,
) {
    fun normalized(): VoiceSettings = copy(
        speakingRate = speakingRate.coerceIn(0.1f, 4.0f),
        pitch = pitch.coerceIn(0.1f, 4.0f),
        styleExaggeration = if (styleExaggeration.isFinite()) styleExaggeration.coerceIn(0.0f, 1.0f) else 0.15f,
    )
}

/**
 * A profile associates a voice with Aura's conversational personality settings
 * without coupling those settings to the AI/reasoning layer.
 */
data class AuraVoiceProfile(
    val profileId: String,
    val personalityId: String?,
    val voiceSettings: VoiceSettings,
)

data class ConversationStateSnapshot(
    val state: ConversationState,
    val error: VoiceError? = null,
)

enum class VoiceOperationStatus {
    ACCEPTED,
    REJECTED,
}

data class VoiceOperationResult(
    val status: VoiceOperationStatus,
    val error: VoiceError? = null,
) {
    val accepted: Boolean
        get() = status == VoiceOperationStatus.ACCEPTED

    companion object {
        fun accepted() = VoiceOperationResult(VoiceOperationStatus.ACCEPTED)

        fun rejected(error: VoiceError) =
            VoiceOperationResult(VoiceOperationStatus.REJECTED, error)
    }
}

