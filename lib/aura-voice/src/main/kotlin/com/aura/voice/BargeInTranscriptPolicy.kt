package com.aura.voice

/** Rejects Aura's own TTS from barge-in and waits for a stable user partial. */
class BargeInTranscriptPolicy {
    private var previousPartial = ""
    private var stablePartialCount = 0

    @Synchronized
    fun shouldInterrupt(partial: String, spokenText: String): Boolean {
        val normalized = normalize(partial)
        if (normalized.isBlank() || isLikelyEcho(spokenText, partial) || normalized in FILLERS) {
            reset()
            return false
        }
        if (normalized.length < MIN_PARTIAL_CHARS) return false

        val continues = previousPartial.isNotBlank() &&
            (normalized.startsWith(previousPartial) || previousPartial.startsWith(normalized))
        stablePartialCount = if (continues) stablePartialCount + 1 else 1
        previousPartial = normalized
        if (stablePartialCount < REQUIRED_STABLE_PARTIALS) return false
        reset()
        return true
    }

    @Synchronized
    fun isLikelyEcho(spokenText: String, transcript: String): Boolean {
        val spoken = normalize(spokenText).split(' ').filter(String::isNotBlank)
        val heard = normalize(transcript).split(' ').filter(String::isNotBlank)
        if (spoken.isEmpty() || heard.isEmpty()) return false
        if (heard.size == 1) return heard.single().length >= 2 && heard.single() in spoken

        val spokenPhrase = spoken.joinToString(" ")
        val heardPhrase = heard.joinToString(" ")
        if (spokenPhrase.contains(heardPhrase)) return true
        if (heard.size == 2) return longestCommonSubsequence(spoken, heard) == heard.size
        if (heard.size < MIN_FUZZY_WORDS) return false
        val minimumMatches = heard.size - MAX_MISHEARD_WORDS
        return longestCommonRun(spoken, heard) >= MIN_COMMON_RUN &&
            longestCommonSubsequence(spoken, heard) >= minimumMatches
    }

    @Synchronized
    fun reset() {
        previousPartial = ""
        stablePartialCount = 0
    }

    private fun longestCommonRun(first: List<String>, second: List<String>): Int {
        var longest = 0
        for (firstStart in first.indices) for (secondStart in second.indices) {
            var run = 0
            while (
                firstStart + run < first.size &&
                secondStart + run < second.size &&
                first[firstStart + run] == second[secondStart + run]
            ) run++
            longest = maxOf(longest, run)
        }
        return longest
    }

    private fun longestCommonSubsequence(first: List<String>, second: List<String>): Int {
        val previous = IntArray(second.size + 1)
        val current = IntArray(second.size + 1)
        for (firstWord in first) {
            for (index in second.indices) {
                current[index + 1] = if (firstWord == second[index]) {
                    previous[index] + 1
                } else {
                    maxOf(previous[index + 1], current[index])
                }
            }
            current.copyInto(previous)
            current.fill(0)
        }
        return previous.last()
    }

    private fun normalize(text: String) = text.lowercase()
        .replace(Regex("\\bi\\s+am\\b"), "im")
        .replace(Regex("['’]"), "")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()
        .replace(Regex("\\s+"), " ")

    private companion object {
        const val MIN_PARTIAL_CHARS = 2
        const val REQUIRED_STABLE_PARTIALS = 2
        const val MIN_FUZZY_WORDS = 3
        const val MIN_COMMON_RUN = 2
        const val MAX_MISHEARD_WORDS = 1
        val FILLERS = setOf("uh", "um", "hmm", "mm")
    }
}
