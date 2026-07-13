package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores a fact/context snippet that AURA remembers across conversations.
 * Examples: "user prefers morning reminders", "user's mom's number is 555-1234".
 */
@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
