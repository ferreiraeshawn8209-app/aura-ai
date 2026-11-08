package com.aura.ai.ui.chat

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechDiagnosticPolicyTest {
    @Test
    fun diagnosticsAreOffByDefaultUnlessExplicitlyEnabled() {
        assertEquals("", SpeechDiagnosticPolicy.format(false, "private words", .7f, "en-ZA"))
        assertEquals("Speech recognition needs attention.", SpeechDiagnosticPolicy.summarizeError(false, "raw provider detail"))
    }

    @Test
    fun optInShowsTranscriptConfidenceLanguageAlternativesAndError() {
        assertEquals(
            "Recognized: open settings · confidence 82% · language en-ZA · alternatives: open settings please | open sittings",
            SpeechDiagnosticPolicy.format(true, " open settings ", .82f, "en-ZA", listOf("open settings", "open settings please", "open sittings")),
        )
        assertEquals("Speech recognition: No speech matched.", SpeechDiagnosticPolicy.summarizeError(true, "No speech matched."))
    }

    @Test
    fun invalidConfidenceIsNotRenderedAsARealScore() {
        assertEquals("Recognized: test", SpeechDiagnosticPolicy.format(true, "test", Float.NaN, null))
    }
}
