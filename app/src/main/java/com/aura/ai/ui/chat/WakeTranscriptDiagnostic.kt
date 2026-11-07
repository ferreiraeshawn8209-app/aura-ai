package com.aura.ai.ui.chat

/** Keeps the exact local wake recognizer output visible while its command is handled. */
internal object WakeTranscriptDiagnostic {
    fun describe(transcript: String?): String = transcript
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?.let { "Wake heard: $it" }
        .orEmpty()
}
