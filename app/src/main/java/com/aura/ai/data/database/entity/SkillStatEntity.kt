package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_stats")
data class SkillStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillName: String,
    val version: String,
    val successfulRuns: Int = 0,
    val failedRuns: Int = 0,
    val averageConfidence: Double = 0.0,
    val lastObservedAt: Long = System.currentTimeMillis()
)
