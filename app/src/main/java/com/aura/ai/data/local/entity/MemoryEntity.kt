package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // PREFERENCE, FACT, WORKFLOW, INSTRUCTION, PROJECT_CONTEXT, USER_SETTING, SKILL_REFERENCE
    val content: String,
    val importance: Int = 5, // 1-10 scale
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis(),
    val metadata: String = "{}" // JSON string for flexible metadata
)
