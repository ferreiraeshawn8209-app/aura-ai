package com.aura.ai.voice.wake

/** Matches Aura's sole wake phrase and returns words spoken after it, if any. */
object WakePhraseMatcher {
    private val prefix = Regex("^\\s*yo(?:[\\W_]+)yo(?:[\\W_]+)yo\\b[\\s,.:;!?-]*", RegexOption.IGNORE_CASE)

    fun commandAfterWake(text: String): String? {
        val match = prefix.find(text) ?: return null
        return text.substring(match.range.last + 1).trim().takeIf(String::isNotEmpty)
    }

    fun matches(text: String): Boolean = prefix.containsMatchIn(text)
}
