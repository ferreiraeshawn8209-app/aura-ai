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

    @Test
    fun includesValidConfidenceWithoutChangingTheTranscript() {
        assertEquals(
            "Wake heard: Yo yo yo, open WhatsApp · confidence 82%",
            WakeTranscriptDiagnostic.describe("Yo yo yo, open WhatsApp", 0.82f),
        )
    }

    @Test
    fun includesOtherRecognizerHypothesesToDiagnoseMisheardWords() {
        assertEquals(
            "Wake heard: Yo yo yo, what is five plus five? · confidence 82% · other guesses: Yo yo yo, what is five plus free? | Yo yo yo, what is five plus nine?",
            WakeTranscriptDiagnostic.describe(
                "Yo yo yo, what is five plus five?",
                0.82f,
                listOf(
                    "Yo yo yo, what is five plus five?",
                    "Yo yo yo, what is five plus free?",
                    "Yo yo yo, what is five plus nine?",
                ),
            ),
        )
    }

    @Test
    fun showsConversationAlternativesInDiagnostic() {
        assertEquals(
            "Recognized (en-ZA, confidence 76%): what is five plus free? · other guesses: what is five plus five?",
            WakeTranscriptDiagnostic.describeConversation(
                transcript = "what is five plus free?",
                languageTag = "en-ZA",
                confidence = 0.76f,
                alternatives = listOf("what is five plus five?"),
            ),
        )
    }
}
