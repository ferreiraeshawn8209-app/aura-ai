package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "skill")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val instructions: String,
    val status: String, // LEARN, UNDERSTAND, PRACTICE, TEST, VERIFY, STORE, USE, EVALUATE, IMPROVE
    val version: String = "1.0.0",
    val testCriteria: String, // JSON array of test criteria
    val verificationCriteria: String, // JSON array of verification criteria
    val evaluationData: String = "{}", // JSON object with success/failure metrics
    val lastEvaluatedTimestamp: Long? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val updatedTimestamp: Long = System.currentTimeMillis()
)
