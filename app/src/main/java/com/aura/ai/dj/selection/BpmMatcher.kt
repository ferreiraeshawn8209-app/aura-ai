package com.aura.ai.dj.selection

import com.aura.ai.dj.domain.Track
import javax.inject.Inject

class BpmMatcher @Inject constructor() {
    fun findCompatibleTracks(
        targetBpm: Int,
        tracks: List<Track>,
        tolerance: Int = 10 // +/- 10 BPM
    ): List<Track> {
        return tracks
            .filter { kotlin.math.abs(it.bpm - targetBpm) <= tolerance }
            .sortedBy { kotlin.math.abs(it.bpm - targetBpm) }
    }

    fun canTransition(currentBpm: Int, nextBpm: Int, tolerance: Int = 10): Boolean {
        return kotlin.math.abs(currentBpm - nextBpm) <= tolerance
    }

    fun calculateBpmRamp(
        startBpm: Int,
        endBpm: Int,
        steps: Int
    ): List<Int> {
        if (steps <= 1) return listOf(startBpm)
        
        val ramp = mutableListOf<Int>()
        val increment = (endBpm - startBpm).toFloat() / (steps - 1)
        
        repeat(steps) { i ->
            ramp.add((startBpm + (increment * i)).toInt())
        }
        
        return ramp
    }
}
