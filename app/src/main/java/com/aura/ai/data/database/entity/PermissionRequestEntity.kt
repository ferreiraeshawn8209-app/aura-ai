package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "permission_requests")
data class PermissionRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val permission: String,
    val reason: String,
    val requestedBy: String,
    val granted: Boolean = false,
    val handledAt: Long? = null,
    val requestedAt: Long = System.currentTimeMillis()
)
