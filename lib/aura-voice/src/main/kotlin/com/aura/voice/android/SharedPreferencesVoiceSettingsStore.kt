package com.aura.voice.android

import android.content.Context
import com.aura.voice.VoiceProviderType
import com.aura.voice.VoiceSettings
import com.aura.voice.VoiceSettingsStore

class SharedPreferencesVoiceSettingsStore(
    context: Context,
    preferencesName: String = "aura_voice_settings",
) : VoiceSettingsStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        preferencesName,
        Context.MODE_PRIVATE,
    )

    override fun load(): VoiceSettings = VoiceSettings(
        provider = preferences.getString(KEY_PROVIDER, VoiceProviderType.ANDROID.name)
            ?.let { runCatching { VoiceProviderType.valueOf(it) }.getOrDefault(VoiceProviderType.ANDROID) }
            ?: VoiceProviderType.ANDROID,
        providerId = preferences.getString(KEY_PROVIDER_ID, null),
        voiceId = preferences.getString(KEY_VOICE_ID, null),
        languageTag = preferences.getString(KEY_LANGUAGE, null)
            .orEmpty()
            .ifBlank { java.util.Locale.getDefault().toLanguageTag() },
        speakingRate = preferences.getFloat(KEY_RATE, 1.0f),
        pitch = preferences.getFloat(KEY_PITCH, 1.0f),
        styleExaggeration = preferences.getFloat(KEY_STYLE, 0.15f),
    ).normalized()

    override fun save(settings: VoiceSettings) {
        preferences.edit()
            .putString(KEY_PROVIDER, settings.provider.name)
            .putString(KEY_PROVIDER_ID, settings.providerId)
            .putString(KEY_VOICE_ID, settings.voiceId)
            .putString(KEY_LANGUAGE, settings.languageTag)
            .putFloat(KEY_RATE, settings.speakingRate)
            .putFloat(KEY_PITCH, settings.pitch)
            .putFloat(KEY_STYLE, settings.normalized().styleExaggeration)
            .apply()
    }

    private companion object {
        const val KEY_PROVIDER = "provider"
        const val KEY_PROVIDER_ID = "provider_id"
        const val KEY_VOICE_ID = "voice_id"
        const val KEY_LANGUAGE = "language_tag"
        const val KEY_RATE = "speaking_rate"
        const val KEY_PITCH = "pitch"
        const val KEY_STYLE = "style_exaggeration"
    }
}