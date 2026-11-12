package com.aura.ai.ui.chat

/** Keeps a clearly incomplete wake-command tail from being sent as a full request. */
internal object WakeCommandContinuationPolicy {
    private val incompleteFragments = setOf(
        "i",
        "i need",
        "i need to",
        "i want",
        "i want to",
        "what",
        "what is",
        "what are",
        "what's",
        "whats",
        "who",
        "who is",
        "who are",
        "where",
        "where is",
        "where are",
        "when",
        "when is",
        "when are",
        "why",
        "why is",
        "why are",
        "how",
        "how do",
        "how does",
        "how can",
        "how much",
        "how many",
        "how long",
        "how far",
        "can you",
        "could you",
        "would you",
        "will you",
        "please",
        "tell me",
        "open",
        "launch",
        "search for",
        "look for",
        "show me",
    )

    fun shouldContinue(command: String): Boolean {
        val normalized = command
            .trim()
            .lowercase()
            .replace('’', '\'')
            .replace(Regex("[.!?,:;…]+$"), "")
            .replace(Regex("\\s+"), " ")
        return normalized in incompleteFragments
    }
}
