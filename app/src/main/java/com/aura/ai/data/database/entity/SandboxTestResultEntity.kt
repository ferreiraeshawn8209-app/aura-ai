package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sandbox_test_results")
data class SandboxTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val skillName: String,
    val version: String,
    val passed: Boolean,
    val metricsJson: String,
    val runAt: Long = System.currentTimeMillis()
)
