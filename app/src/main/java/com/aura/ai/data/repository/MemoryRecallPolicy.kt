package com.aura.ai.data.repository

import com.aura.ai.data.local.entity.MemoryEntity

/** Bounded word-overlap retrieval for the existing local memory table. */
internal object MemoryRecallPolicy {
    private val stopWords = setOf(
        "about", "after", "again", "also", "and", "are", "can", "could", "did", "does", "for",
        "from", "have", "help", "hey", "how", "into", "just", "like", "make", "me", "my", "please",
        "remember", "same", "tell", "that", "the", "their", "them", "then", "there", "these", "they",
        "this", "those", "what", "when", "where", "which", "who", "why", "with", "would", "you", "your",
    )
    private val tokenPattern = Regex("[\\p{L}\\p{N}]{3,}")

    fun queryTerms(query: String): List<String> = tokenPattern.findAll(query.lowercase())
        .map { normalize(it.value) }
        .filter { it.length >= 3 && it !in stopWords }
        .distinct()
        .take(8)
        .toList()

    fun rank(query: String, candidates: Collection<MemoryEntity>, limit: Int = 12): List<MemoryEntity> {
        if (limit <= 0 || candidates.isEmpty()) return emptyList()
        val terms = queryTerms(query)
        val pinned = candidates.asSequence()
            .filter { it.type.uppercase() in PINNED_TYPES }
            .sortedWith(compareByDescending<MemoryEntity> { it.importance }.thenByDescending { it.updatedTimestamp })
            .take(MAX_PINNED)
            .toList()
        if (terms.isEmpty()) return (pinned + candidates.sortedByDescending { it.importance })
            .distinctBy { it.id }
            .take(limit)

        val relevant = candidates.asSequence()
            .map { memory -> memory to terms.count { term -> containsTerm(memory.content, term) } }
            .filter { (_, hits) -> hits > 0 }
            .sortedWith(
                compareByDescending<Pair<MemoryEntity, Int>> { it.second }
                    .thenByDescending { it.first.importance }
                    .thenByDescending { it.first.updatedTimestamp },
            )
            .map { it.first }
            .toList()
        return (pinned + relevant).distinctBy { it.id }.take(limit)
    }

    private fun containsTerm(content: String, term: String): Boolean {
        val contentTerms = tokenPattern.findAll(content.lowercase()).map { normalize(it.value) }
        return contentTerms.any { it == term }
    }

    private fun normalize(token: String): String =
        if (token.length > 4 && token.endsWith('s')) token.dropLast(1) else token

    private val PINNED_TYPES = setOf("PREFERENCE", "USER_SETTING", "INSTRUCTION", "PROJECT_CONTEXT")
    private const val MAX_PINNED = 3
}
