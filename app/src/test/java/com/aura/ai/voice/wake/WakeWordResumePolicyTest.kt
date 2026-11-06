package com.aura.ai.voice.wake

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeWordResumePolicyTest {
    @Test
    fun wakeTriggeredActivityDoesNotRestartStandbyListener() {
        assertFalse(
            WakeWordResumePolicy.shouldStartStandby(
                wakeEventPending = true,
                wakeWordEnabled = true,
                microphonePermissionGranted = true,
            ),
        )
    }

    @Test
    fun normalActivityStartMayResumeOptedInStandbyListener() {
        assertTrue(
            WakeWordResumePolicy.shouldStartStandby(
                wakeEventPending = false,
                wakeWordEnabled = true,
                microphonePermissionGranted = true,
            ),
        )
    }

    @Test
    fun resumesOnlyWhenWakeModeAndMicrophonePermissionAreEnabled() {
        assertTrue(
            WakeWordResumePolicy.shouldResume(
                wakeWordEnabled = true,
                microphonePermissionGranted = true,
                handsFreeSessionActive = false,
            ),
        )
        assertFalse(
            WakeWordResumePolicy.shouldResume(
                wakeWordEnabled = false,
                microphonePermissionGranted = true,
                handsFreeSessionActive = false,
            ),
        )
        assertFalse(
            WakeWordResumePolicy.shouldResume(
                wakeWordEnabled = true,
                microphonePermissionGranted = false,
                handsFreeSessionActive = false,
            ),
        )
        assertFalse(
            WakeWordResumePolicy.shouldResume(
                wakeWordEnabled = true,
                microphonePermissionGranted = true,
                handsFreeSessionActive = true,
            ),
        )
        assertFalse(
            WakeWordResumePolicy.shouldResume(
                wakeWordEnabled = false,
                microphonePermissionGranted = false,
                handsFreeSessionActive = false,
            ),
        )
    }
}
