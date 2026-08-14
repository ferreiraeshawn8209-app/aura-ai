package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val eventSource: String,
    val detailsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
