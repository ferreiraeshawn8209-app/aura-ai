package com.aura.ai.ui.chat

/** Selects a few older user/assistant exchanges that overlap with a new request. */
internal object ConversationRecallPolicy {
    private val stopWords = setOf(
        "about", "after", "again", "also", "and", "are", "can", "could", "did", "does", "for", "from",
        "have", "help", "how", "into", "just", "like", "make", "me", "my", "please", "same", "tell",
        "that", "the", "their", "them", "then", "there", "these", "they", "this", "those", "what", "when",
        "where", "which", "who", "why", "with", "would", "you", "your",
    )
    private val tokenPattern = Regex("[\\p{L}\\p{N}]{3,}")

    fun relevantExchanges(
        request: String,
        olderTurns: List<ChatTurn>,
        maxExchanges: Int = 3,
        maxCharacters: Int = 1_800,
    ): List<Pair<String, String>> {
        if (maxExchanges <= 0 || maxCharacters <= 0) return emptyList()
        val queryTerms = terms(request)
        if (queryTerms.isEmpty()) return emptyList()

        val exchanges = mutableListOf<ScoredExchange>()
        var index = 0
        while (index + 1 < olderTurns.size) {
            val user = olderTurns[index]
            val assistant = olderTurns[index + 1]
            if (user.isUser && !user.error && !user.streaming && assistant.id != user.id &&
                !assistant.isUser && !assistant.error && !assistant.streaming &&
                user.text.isNotBlank() && assistant.text.isNotBlank()
            ) {
                val content = "${user.text} ${assistant.text}"
                val hits = queryTerms.count { it in terms(content) }
                if (hits > 0) exchanges += ScoredExchange(index, hits, user.text.trim(), assistant.text.trim())
                index += 2
            } else {
                index++
            }
        }

        val selected = exchanges.sortedWith(
            compareByDescending<ScoredExchange> { it.hits }.thenByDescending { it.index },
        ).take(maxExchanges).sortedBy { it.index }

        val result = mutableListOf<Pair<String, String>>()
        var remaining = maxCharacters
        for (exchange in selected) {
            if (remaining < MIN_EXCHANGE_CHARS) break
            val userText = exchange.user.take((remaining / 2).coerceAtMost(MAX_MESSAGE_CHARS))
            val assistantText = exchange.assistant.take((remaining - userText.length).coerceAtMost(MAX_MESSAGE_CHARS))
            if (userText.isBlank() || assistantText.isBlank()) continue
            result += "user" to userText
            result += "assistant" to assistantText
            remaining -= userText.length + assistantText.length
        }
        return result
    }

    private fun terms(text: String): Set<String> = tokenPattern.findAll(text.lowercase())
        .map { match -> if (match.value.length > 4 && match.value.endsWith('s')) match.value.dropLast(1) else match.value }
        .filter { it !in stopWords }
        .toSet()

    private data class ScoredExchange(val index: Int, val hits: Int, val user: String, val assistant: String)

    private const val MAX_MESSAGE_CHARS = 900
    private const val MIN_EXCHANGE_CHARS = 80
}
