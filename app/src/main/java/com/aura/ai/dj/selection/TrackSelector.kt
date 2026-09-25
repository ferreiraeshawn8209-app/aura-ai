package com.aura.ai.dj.selection

import com.aura.ai.dj.domain.DjMode
import com.aura.ai.dj.domain.Track
import javax.inject.Inject

class TrackSelector @Inject constructor() {
    fun selectNextTrack(
        currentTrack: Track,
        availableTracks: List<Track>,
        mode: DjMode,
        energyTarget: Int? = null
    ): Track? {
        if (availableTracks.isEmpty()) return null

        val candidateTracks = when (mode) {
            DjMode.CURRENT_VIBE -> selectByVibe(currentTrack, availableTracks)
            DjMode.ENERGY_UP -> selectByEnergyIncrease(currentTrack, availableTracks)
            DjMode.ENERGY_DOWN -> selectByEnergyDecrease(currentTrack, availableTracks)
            DjMode.WEDDING_BACKGROUND -> selectWeddingTrack(availableTracks)
            DjMode.CORPORATE -> selectCorporateTrack(availableTracks)
            DjMode.PARTY_OPEN_FORMAT -> selectPartyTrack(availableTracks)
            DjMode.CUSTOM -> selectCustom(currentTrack, availableTracks, energyTarget)
        }

        return candidateTracks.firstOrNull()
    }

    private fun selectByVibe(current: Track, available: List<Track>): List<Track> {
        return available
            .sortedBy { kotlin.math.abs(it.energy - current.energy) }
    }

    private fun selectByEnergyIncrease(current: Track, available: List<Track>): List<Track> {
        return available
            .filter { it.energy > current.energy }
            .sortedBy { it.energy - current.energy }
    }

    private fun selectByEnergyDecrease(current: Track, available: List<Track>): List<Track> {
        return available
            .filter { it.energy < current.energy }
            .sortedByDescending { current.energy - it.energy }
    }

    private fun selectWeddingTrack(available: List<Track>): List<Track> {
        return available
            .filter { it.energy in 3..6 }
            .sortedBy { it.bpm }
    }

    private fun selectCorporateTrack(available: List<Track>): List<Track> {
        return available
            .filter { it.energy in 2..5 }
            .sortedBy { it.bpm }
    }

    private fun selectPartyTrack(available: List<Track>): List<Track> {
        return available
            .filter { it.energy >= 7 }
            .sortedByDescending { it.energy }
    }

    private fun selectCustom(
        current: Track,
        available: List<Track>,
        energyTarget: Int?
    ): List<Track> {
        return if (energyTarget != null) {
            available.sortedBy { kotlin.math.abs(it.energy - energyTarget) }
        } else {
            selectByVibe(current, available)
        }
    }
}
