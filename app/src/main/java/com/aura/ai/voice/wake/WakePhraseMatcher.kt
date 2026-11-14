package com.aura.ai.voice.wake

/** Matches Aura's sole wake phrase and returns words spoken after it, if any. */
object WakePhraseMatcher {
    // Offline speech recognition may render the spoken "yo" as a close homophone.
    // Require three consecutive wake-like tokens at the start to limit false activations.
    private const val WAKE_TOKEN = "(?:yo|yoh|yoo|you|yah|ya|yeh|yeah)"
    private val prefix = Regex(
        "^\\s*(?:$WAKE_TOKEN(?:[\\W_]+$WAKE_TOKEN){2}|yoyoyo)\\b[\\s,.:;!?-]*",
        RegexOption.IGNORE_CASE,
    )

    data class Match(
        val transcript: String,
        val confidence: Float?,
        val alternatives: List<String> = emptyList(),
    )

    fun firstMatchingCandidate(transcripts: List<String>?, confidenceScores: FloatArray?): Match? {
        val match = transcripts
            ?.withIndex()
            ?.firstOrNull { (_, transcript) -> matches(transcript) }
            ?: return null
        val confidence = confidenceScores
            ?.getOrNull(match.index)
            ?.takeIf { it in 0f..1f }
        val transcript = match.value
        val alternatives = transcripts
            .orEmpty()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != transcript }
            .distinct()
            .take(3)
            .toList()
        return Match(transcript, confidence, alternatives)
    }

    fun commandAfterWake(text: String): String? {
        val match = prefix.find(text) ?: return null
        return text.substring(match.range.last + 1).trim().takeIf(String::isNotEmpty)
    }

    fun matches(text: String): Boolean = prefix.containsMatchIn(text)
}
