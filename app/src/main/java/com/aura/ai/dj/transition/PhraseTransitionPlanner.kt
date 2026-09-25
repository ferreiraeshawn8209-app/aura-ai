package com.aura.ai.dj.transition

import com.aura.ai.dj.domain.Track
import com.aura.ai.dj.domain.TransitionPlan
import com.aura.ai.dj.selection.BpmMatcher
import com.aura.ai.dj.selection.KeyMatcher
import javax.inject.Inject

class PhraseTransitionPlanner @Inject constructor(
    private val bpmMatcher: BpmMatcher,
    private val keyMatcher: KeyMatcher
) {
    fun planTransition(
        currentTrack: Track,
        nextTrack: Track
    ): TransitionPlan {
        val bpmMatch = bpmMatcher.canTransition(currentTrack.bpm, nextTrack.bpm)
        val keyAware = keyMatcher.areKeysCompatible(currentTrack.key, nextTrack.key)
        val phraseAligned = calculatePhraseAlignment(currentTrack, nextTrack)
        val crossfadeDuration = calculateCrossfadeDuration(currentTrack, bpmMatch)

        return TransitionPlan(
            currentTrack = currentTrack,
            nextTrack = nextTrack,
            crossfadeDuration = crossfadeDuration,
            bpmMatch = bpmMatch,
            keyAware = keyAware,
            phraseAligned = phraseAligned
        )
    }

    private fun calculatePhraseAlignment(
        currentTrack: Track,
        nextTrack: Track
    ): Boolean {
        // Simple implementation: check if both have beat grid info
        return currentTrack.beatGrid != null && nextTrack.beatGrid != null
    }

    private fun calculateCrossfadeDuration(
        currentTrack: Track,
        bpmMatch: Boolean
    ): Long {
        // Shorter fade if BPM matches, longer if it doesn't
        return if (bpmMatch) 3000L else 5000L // milliseconds
    }

    fun calculateOutroOverlapTime(
        currentTrack: Track,
        nextTrack: Track
    ): Long {
        val currentOutro = currentTrack.outro ?: 0L
        val nextIntro = nextTrack.intro ?: 0L
        return minOf(currentOutro, nextIntro)
    }
}
