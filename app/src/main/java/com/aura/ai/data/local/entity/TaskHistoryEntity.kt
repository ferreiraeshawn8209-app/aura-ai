package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A record of an actual agent run. Captures the request, the plan that was
 * produced, the observed outcome of each step, and whether the task was
 * genuinely verified as successful (never assumed).
 */
@Entity(tableName = "task_history")
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val request: String,
    val planJson: String,
    val outcomeJson: String,
    val outcome: TaskOutcome,
    val skillId: Long? = null,
    val startedAt: Long,
    val finishedAt: Long,
)

enum class TaskOutcome {
    SUCCESS,
    PARTIAL,
    FAILED,
    ABORTED,
}
