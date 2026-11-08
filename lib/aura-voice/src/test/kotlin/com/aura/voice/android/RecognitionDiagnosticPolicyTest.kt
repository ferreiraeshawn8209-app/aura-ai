package com.aura.voice.android

import android.speech.SpeechRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecognitionDiagnosticPolicyTest {
    @Test
    fun confidenceOnlyAcceptsFiniteNormalizedScores() {
        assertEquals(0.82f, RecognitionDiagnosticPolicy.confidence(0.82f))
        assertNull(RecognitionDiagnosticPolicy.confidence(null))
        assertNull(RecognitionDiagnosticPolicy.confidence(-1f))
        assertNull(RecognitionDiagnosticPolicy.confidence(1.1f))
        assertNull(RecognitionDiagnosticPolicy.confidence(Float.NaN))
        assertNull(RecognitionDiagnosticPolicy.confidence(Float.POSITIVE_INFINITY))
    }

    @Test
    fun alternativesAreTrimmedDeduplicatedCaseInsensitivelyAndBounded() {
        assertEquals(
            listOf("Open Settings", "Open setings", "Open settings please"),
            RecognitionDiagnosticPolicy.alternatives(
                listOf("Open Settings", "open settings", "Open setings", "Open settings please", "fourth"),
                "open settings",
            ),
        )
    }

    @Test
    fun errorsAreActionableAndDoNotIncludeTranscriptOrProviderPayload() {
        assertEquals("No speech was recognized. Speak clearly and try again.", RecognitionDiagnosticPolicy.errorMessage(SpeechRecognizer.ERROR_NO_MATCH))
        assertEquals("Microphone permission is required for speech recognition.", RecognitionDiagnosticPolicy.errorMessage(SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS))
        assertEquals("Speech recognition failed (Android error 9876). Try again or check speech-service settings.", RecognitionDiagnosticPolicy.errorMessage(9876))
    }
}
