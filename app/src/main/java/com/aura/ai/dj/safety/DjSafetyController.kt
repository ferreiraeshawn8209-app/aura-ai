package com.aura.ai.dj.safety

import com.aura.ai.dj.core.DjAutoPilotEngine
import com.aura.ai.dj.playback.PlaybackStateProvider
import javax.inject.Inject

class DjSafetyController @Inject constructor(
    private val djEngine: DjAutoPilotEngine,
    private val playbackStateProvider: PlaybackStateProvider
) {
    suspend fun ensureSafePlayback() {
        // Verify automation is running safely
        if (!djEngine.isRunning()) {
            djEngine.pause()
        }
    }

    suspend fun finishCurrentTransition() {
        // Complete any in-progress transition before stopping
        // This is handled by the TransitionPlanner
    }

    suspend fun takeControl() {
        // Immediately cease autonomous decision-making
        djEngine.pause()
    }

    fun isSafeToAutomate(): Boolean {
        return djEngine.isRunning() && playbackStateProvider.getIsPlaying()
    }
}
