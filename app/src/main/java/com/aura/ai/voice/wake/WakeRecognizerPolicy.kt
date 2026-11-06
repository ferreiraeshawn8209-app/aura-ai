package com.aura.ai.voice.wake

/** Chooses Android's local speech recognizer only when the platform supports it. */
internal object WakeRecognizerPolicy {
    fun useOnDeviceRecognizer(apiLevel: Int, onDeviceRecognitionAvailable: Boolean): Boolean =
        apiLevel >= 31 && onDeviceRecognitionAvailable
}
