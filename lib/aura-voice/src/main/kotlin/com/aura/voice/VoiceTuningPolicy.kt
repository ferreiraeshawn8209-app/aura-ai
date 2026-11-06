package com.aura.voice

/** Keeps user-facing Android voice tuning in a natural range. */
object VoiceTuningPolicy {
    const val PITCH_MIN = 0.70f
    const val PITCH_MAX = 1.30f
    const val RATE_MIN = 0.85f
    const val RATE_MAX = 1.15f

    fun apply(settings: VoiceSettings, pitch: Float, speakingRate: Float): VoiceSettings =
        settings.copy(
            pitch = pitch.coerceIn(PITCH_MIN, PITCH_MAX),
            speakingRate = speakingRate.coerceIn(RATE_MIN, RATE_MAX),
        ).normalized()
}
