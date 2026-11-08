package com.aura.ai.ui.chat

/** Formats on-screen ASR diagnostics without writing transcript data to logs or storage. */
internal object SpeechDiagnosticPolicy {
    fun format(
        enabled: Boolean,
        transcript: String?,
        confidence: Float?,
        languageTag: String?,
        alternatives: List<String> = emptyList(),
        errorMessage: String? = null,
    ): String {
        if (!enabled) return ""
        errorMessage?.trim()?.takeIf(String::isNotEmpty)?.let { return "Speech recognition: $it" }
        val text = transcript?.trim()?.takeIf(String::isNotEmpty) ?: return "Speech recognition returned no transcript."
        val confidenceText = confidence?.takeIf { it.isFinite() && it in 0f..1f }?.let { " · confidence ${(it * 100).toInt()}%" }.orEmpty()
        val language = languageTag?.trim()?.takeIf(String::isNotEmpty)?.let { " · language $it" }.orEmpty()
        val other = alternatives.asSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.equals(text, ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(3)
            .toList()
            .let { if (it.isEmpty()) "" else " · alternatives: ${it.joinToString(" | ")}" }
        return "Recognized: $text$confidenceText$language$other"
    }

    fun summarizeError(enabled: Boolean, errorMessage: String): String =
        if (enabled) "Speech recognition: ${errorMessage.trim()}" else "Speech recognition needs attention."
}
