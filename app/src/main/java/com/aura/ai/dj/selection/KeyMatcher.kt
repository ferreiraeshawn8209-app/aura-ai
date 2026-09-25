package com.aura.ai.dj.selection

import com.aura.ai.dj.domain.Track
import javax.inject.Inject

class KeyMatcher @Inject constructor() {
    private val compatibleKeys = mapOf(
        "C" to listOf("C", "G", "F", "Am", "Em"),
        "G" to listOf("G", "D", "C", "Em", "Bm"),
        "D" to listOf("D", "A", "G", "Bm", "F#m"),
        "A" to listOf("A", "E", "D", "F#m", "C#m"),
        "E" to listOf("E", "B", "A", "C#m", "G#m"),
        "B" to listOf("B", "F#", "E", "G#m", "D#m"),
        "F#" to listOf("F#", "C#", "B", "D#m", "A#m"),
        "C#" to listOf("C#", "G#", "F#", "A#m", "E#m"),
        "F" to listOf("F", "C", "Bb", "Dm", "Am"),
        "Bb" to listOf("Bb", "F", "Eb", "Gm", "Dm"),
        "Eb" to listOf("Eb", "Bb", "Ab", "Cm", "Gm"),
        "Ab" to listOf("Ab", "Eb", "Db", "Fm", "Cm"),
        "Db" to listOf("Db", "Ab", "Gb", "Bbm", "Fm"),
        "Gb" to listOf("Gb", "Db", "Bm", "Ebm", "Bbm"),
        "Am" to listOf("Am", "Em", "Dm", "C", "G"),
        "Em" to listOf("Em", "Bm", "Am", "G", "D"),
        "Bm" to listOf("Bm", "F#m", "Em", "D", "A")
    )

    fun areKeysCompatible(key1: String?, key2: String?): Boolean {
        if (key1 == null || key2 == null) return true
        return compatibleKeys[key1]?.contains(key2) ?: false
    }

    fun findCompatibleTracks(
        targetKey: String?,
        tracks: List<Track>
    ): List<Track> {
        if (targetKey == null) return tracks
        
        return tracks.sortedBy { track ->
            if (areKeysCompatible(targetKey, track.key)) 0 else 1
        }
    }
}
