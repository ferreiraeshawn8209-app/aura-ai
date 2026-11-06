package com.aura.ai.voice.wake

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeRecognizerPolicyTest {
    @Test
    fun usesOnDeviceRecognizerWhenSupportedAndAvailable() {
        assertTrue(WakeRecognizerPolicy.useOnDeviceRecognizer(apiLevel = 31, onDeviceRecognitionAvailable = true))
        assertTrue(WakeRecognizerPolicy.useOnDeviceRecognizer(apiLevel = 35, onDeviceRecognitionAvailable = true))
    }

    @Test
    fun usesSystemRecognizerWhenPlatformIsOldOrLocalEngineIsUnavailable() {
        assertFalse(WakeRecognizerPolicy.useOnDeviceRecognizer(apiLevel = 30, onDeviceRecognitionAvailable = true))
        assertFalse(WakeRecognizerPolicy.useOnDeviceRecognizer(apiLevel = 34, onDeviceRecognitionAvailable = false))
    }
}
