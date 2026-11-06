package com.aura.ai.voice.wake

/** Keeps standby wake listening opt-in and avoids starting its microphone without permission. */
internal object WakeWordResumePolicy {
    fun shouldStartStandby(
        wakeEventPending: Boolean,
        wakeWordEnabled: Boolean,
        microphonePermissionGranted: Boolean,
    ): Boolean = !wakeEventPending && wakeWordEnabled && microphonePermissionGranted

    fun shouldResume(
        wakeWordEnabled: Boolean,
        microphonePermissionGranted: Boolean,
        handsFreeSessionActive: Boolean,
    ): Boolean = wakeWordEnabled && microphonePermissionGranted && !handsFreeSessionActive
}
