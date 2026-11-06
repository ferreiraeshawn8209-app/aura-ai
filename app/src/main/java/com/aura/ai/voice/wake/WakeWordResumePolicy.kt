package com.aura.ai.voice.wake

/** Keeps standby wake listening opt-in and avoids starting its microphone without permission. */
internal object WakeWordResumePolicy {
    fun shouldResume(wakeWordEnabled: Boolean, microphonePermissionGranted: Boolean): Boolean =
        wakeWordEnabled && microphonePermissionGranted
}
