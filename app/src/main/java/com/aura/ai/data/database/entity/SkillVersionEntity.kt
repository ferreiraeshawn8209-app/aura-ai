package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_versions")
data class SkillVersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillName: String,
    val version: String,
    val metadataJson: String,
    val codeRef: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val testPassed: Boolean = false
)
