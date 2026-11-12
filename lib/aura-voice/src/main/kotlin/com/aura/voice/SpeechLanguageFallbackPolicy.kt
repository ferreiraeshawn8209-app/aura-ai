package com.aura.voice

/**
 * Small, deterministic fallback list for Android recognizers that reject
 * South-African English because the corresponding language pack is absent.
 * This affects speech recognition only; it does not change Aura's TTS voice.
 */
object SpeechLanguageFallbackPolicy {
    fun nextLanguage(currentLanguageTag: String): String? {
        val normalized = currentLanguageTag.trim().replace('_', '-').lowercase()
        return when (normalized) {
            "en-za" -> "en-GB"
            "en-gb" -> "en-US"
            else -> null
        }
    }
}
