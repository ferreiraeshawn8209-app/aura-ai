package com.aura.ai.agent.tool

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SharedFileSearchPolicyTest {
    private val report = SharedFileEntry("doc-1", "report.pdf", "Clients/2026/report.pdf", "application/pdf")
    private val backup = SharedFileEntry("doc-2", "report.pdf", "Archive/report.pdf", "application/pdf")

    @Test
    fun requiresUsefulFilenameQueryAndRejectsControlCharacters() {
        assertNull(SharedFileSearchPolicy.query("a"))
        assertNull(SharedFileSearchPolicy.query("  "))
        assertNull(SharedFileSearchPolicy.query("notes\nsecret"))
        assertEquals("invoice", SharedFileSearchPolicy.query(" invoice "))
    }

    @Test
    fun matchesFilenameOrRelativePathWithoutCaseSensitivity() {
        assertTrue(SharedFileSearchPolicy.matches(report, "REPORT"))
        assertTrue(SharedFileSearchPolicy.matches(report, "clients/2026"))
    }

    @Test
    fun exactRelativePathDisambiguatesDuplicateNames() {
        assertEquals(listOf(report), SharedFileSearchPolicy.selectUnique(listOf(report, backup), "Clients/2026/report.pdf"))
        assertEquals(listOf(report, backup), SharedFileSearchPolicy.selectUnique(listOf(report, backup), "report.pdf"))
    }
}
