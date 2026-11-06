package com.aura.ai.data.repository

import com.aura.ai.data.local.entity.MemoryEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryRecallPolicyTest {
    @Test
    fun matchesMeaningfulWordsInsteadOfRequiringTheWholeQuestionAsASubstring() {
        val business = memory(1, "FACT", "The user's company is BeatKulture, an event business.")
        val unrelated = memory(2, "TASK", "Completed: organize duplicate music tracks.")

        val recalled = MemoryRecallPolicy.rank("What is my company called?", listOf(unrelated, business))

        assertEquals(listOf(business), recalled)
    }

    @Test
    fun keepsImportantPreferencesAvailableAlongsideRelevantMatches() {
        val preference = memory(1, "PREFERENCE", "The user prefers concise everyday English.", importance = 8)
        val project = memory(2, "PROJECT_CONTEXT", "Aura is the user's assistant project.", importance = 9)
        val unrelated = memory(3, "TASK", "Completed: sort duplicate music files.", importance = 10)

        val recalled = MemoryRecallPolicy.rank("Find the duplicate music tracks", listOf(preference, project, unrelated))

        assertTrue(recalled.take(2).contains(preference))
        assertTrue(recalled.contains(unrelated))
    }

    @Test
    fun removesDuplicateCandidatesAndBoundsResultCount() {
        val candidates = (1L..20L).map { memory(it, "FACT", "Aura project fact number $it") }

        val recalled = MemoryRecallPolicy.rank("Tell me about the Aura project", candidates + candidates, limit = 5)

        assertEquals(5, recalled.size)
        assertEquals(5, recalled.map { it.id }.distinct().size)
    }

    private fun memory(id: Long, type: String, content: String, importance: Int = 5) =
        MemoryEntity(id = id, type = type, content = content, importance = importance, updatedTimestamp = id)
}
