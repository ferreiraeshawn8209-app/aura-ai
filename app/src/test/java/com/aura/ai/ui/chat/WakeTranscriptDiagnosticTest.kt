package com.aura.ai.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class WakeTranscriptDiagnosticTest {
    @Test
    fun showsFullWakeAndCommandTranscript() {
        assertEquals(
            "Wake heard: Yo yo yo, what is five plus five?",
            WakeTranscriptDiagnostic.describe(" Yo yo yo, what is five plus five? "),
        )
    }

    @Test
    fun omitsDiagnosticWhenThereIsNoWakeTranscript() {
        assertEquals("", WakeTranscriptDiagnostic.describe(null))
        assertEquals("", WakeTranscriptDiagnostic.describe("  "))
    }
}
