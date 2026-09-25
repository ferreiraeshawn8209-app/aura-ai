package com.aura.ai.dj.transition

import javax.inject.Inject

class CrossfadeController @Inject constructor() {
    fun calculateCrossfadeVolumes(
        elapsedTime: Long,
        totalDuration: Long
    ): Pair<Float, Float> {
        val progress = (elapsedTime.toFloat() / totalDuration).coerceIn(0f, 1f)
        val currentVolume = (1f - progress).coerceIn(0f, 1f)
        val nextVolume = progress.coerceIn(0f, 1f)
        return Pair(currentVolume, nextVolume)
    }

    fun isCrossfadeComplete(
        elapsedTime: Long,
        totalDuration: Long
    ): Boolean {
        return elapsedTime >= totalDuration
    }
}
