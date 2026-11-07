package com.aura.ai.ui.chat

/** Keeps the exact local wake recognizer output visible while its command is handled. */
internal object WakeTranscriptDiagnostic {
    fun describe(
        transcript: String?,
        confidence: Float? = null,
        alternatives: List<String> = emptyList(),
    ): String = transcript
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.let {
            val confidenceLabel = confidence
                ?.takeIf { score -> score in 0f..1f }
                ?.let { score -> " · confidence ${(score * 100).toInt()}%" }
                .orEmpty()
            val alternativesLabel = alternatives
                .asSequence()
                .map { it.trim() }
                .filter { candidate -> candidate.isNotEmpty() && candidate != it }
                .distinct()
                .take(3)
                .toList()
                .let { candidates ->
                    if (candidates.isEmpty()) "" else candidates.joinToString(separator = " | ", prefix = " · other guesses: ")
                }
                .orEmpty()
            "Wake heard: $it$confidenceLabel$alternativesLabel"
        }
        .orEmpty()

    fun describeConversation(
        transcript: String,
        languageTag: String?,
        confidence: Float?,
        alternatives: List<String>,
    ): String {
        val confidenceLabel = confidence
            ?.takeIf { it in 0f..1f }
            ?.let { "${(it * 100).toInt()}%" }
            ?: "not provided"
        val label = "Recognized (${languageTag ?: "en-ZA"}, confidence $confidenceLabel): $transcript"
        val otherGuesses = alternatives
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != transcript.trim() }
            .distinct()
            .take(3)
            .toList()
        return if (otherGuesses.isEmpty()) label else "$label · other guesses: ${otherGuesses.joinToString(" | ")}"
    }
}

