package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "agent_action")
data class AgentActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val requestId: String, // Unique request identifier
    val tool: String,
    val status: String, // REQUESTED, PLANNED, AWAITING_PERMISSION, EXECUTING, VERIFYING, SUCCEEDED, FAILED, CANCELLED
    val parameters: String, // JSON string
    val result: String? = null, // JSON string
    val errorMessage: String? = null,
    val executionTimeMs: Long? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long? = null
)
