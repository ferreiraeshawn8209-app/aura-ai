package com.aura.voice.android

/** Safe, concise recognition diagnostics. Transcript text is never persisted or logged here. */
internal object RecognitionDiagnosticPolicy {
    fun confidence(value: Float?): Float? = value?.takeIf { it.isFinite() && it in 0f..1f }

    fun alternatives(values: List<String>?, primary: String): List<String> = values.orEmpty()
        .asSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.equals(primary.trim(), ignoreCase = true) }
        .distinctBy { it.lowercase() }
        .take(MAX_ALTERNATIVES)
        .toList()

    fun errorMessage(code: Int): String = when (code) {
        android.speech.SpeechRecognizer.ERROR_AUDIO -> "The microphone audio could not be captured. Check microphone access and try again."
        android.speech.SpeechRecognizer.ERROR_CLIENT -> "Speech recognition was interrupted. Try speaking again."
        android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for speech recognition."
        android.speech.SpeechRecognizer.ERROR_NETWORK -> "The speech recognition service could not connect to its network service."
        android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "The speech recognition service timed out. Try again when the connection is stable."
        android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized. Speak clearly and try again."
        android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition is busy. Aura will retry shortly."
        android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected. Try again when ready."
        android.speech.SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        android.speech.SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "The selected recognition language is unavailable on this device. Check Android speech language settings."
        else -> "Speech recognition failed (Android error $code). Try again or check speech-service settings."
    }

    const val MAX_ALTERNATIVES = 3
}
