package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_events")
data class LearningEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val sourceSkill: String?,
    val detailsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)
