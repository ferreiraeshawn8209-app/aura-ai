package com.aura.ai.dj.logging

import com.aura.ai.core.logging.T1000Logger
import com.aura.ai.dj.domain.DjMode
import com.aura.ai.dj.domain.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DjSessionLogger @Inject constructor(
    private val logger: T1000Logger
) {
    fun logSessionStart(sessionId: String, mode: DjMode, duration: Long) {
        logger.logOllamaEvent("DJ Session started: $sessionId, mode=$mode, duration=${duration}ms")
    }

    fun logSessionEnd(sessionId: String) {
        logger.logOllamaEvent("DJ Session ended: $sessionId")
    }

    fun logTrackPlay(track: Track, index: Int, total: Int) {
        logger.logOllamaEvent("Now playing: ${track.title} by ${track.artist} ($index/$total)")
    }

    fun logTransition(fromTrack: Track, toTrack: Track) {
        logger.logOllamaEvent("Transition: ${fromTrack.title} -> ${toTrack.title}")
    }

    fun logSkip(track: Track) {
        logger.logOllamaEvent("Skipped: ${track.title}")
    }

    fun logManualTakeover() {
        logger.logOllamaEvent("Manual takeover: automation paused")
    }

    fun logError(errorMessage: String) {
        logger.e("DJ", "DJ Error: $errorMessage")
    }
}
