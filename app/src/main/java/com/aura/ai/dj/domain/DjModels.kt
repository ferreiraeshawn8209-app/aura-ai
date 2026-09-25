package com.aura.ai.dj.domain

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long, // milliseconds
    val bpm: Int,
    val key: String? = null,
    val genre: String? = null,
    val energy: Int, // 1-10 scale
    val playbackPosition: Long = 0,
    val beatGrid: String? = null,
    val intro: Long? = null, // milliseconds
    val outro: Long? = null // milliseconds
)

@Serializable
data class DjSession(
    val id: String,
    val mode: DjMode,
    val duration: Long, // milliseconds
    val startTime: Long,
    val currentTrackIndex: Int = 0,
    val queue: List<Track> = emptyList(),
    val isRunning: Boolean = false,
    val remainingTime: Long = 0
)

enum class DjMode {
    CURRENT_VIBE,
    ENERGY_UP,
    ENERGY_DOWN,
    WEDDING_BACKGROUND,
    CORPORATE,
    PARTY_OPEN_FORMAT,
    CUSTOM
}

enum class DjAutomationState {
    IDLE,
    SELECTING_TRACK,
    TRANSITIONING,
    PLAYING,
    PAUSED
}

@Serializable
data class TransitionPlan(
    val currentTrack: Track,
    val nextTrack: Track,
    val crossfadeDuration: Long, // milliseconds
    val bpmMatch: Boolean,
    val keyAware: Boolean,
    val phraseAligned: Boolean
)
