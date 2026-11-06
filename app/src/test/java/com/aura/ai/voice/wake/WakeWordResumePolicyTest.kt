package com.aura.ai.voice.wake

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeWordResumePolicyTest {
    @Test
    fun resumesOnlyWhenWakeModeAndMicrophonePermissionAreEnabled() {
        assertTrue(WakeWordResumePolicy.shouldResume(wakeWordEnabled = true, microphonePermissionGranted = true))
        assertFalse(WakeWordResumePolicy.shouldResume(wakeWordEnabled = false, microphonePermissionGranted = true))
        assertFalse(WakeWordResumePolicy.shouldResume(wakeWordEnabled = true, microphonePermissionGranted = false))
        assertFalse(WakeWordResumePolicy.shouldResume(wakeWordEnabled = false, microphonePermissionGranted = false))
    }
}
