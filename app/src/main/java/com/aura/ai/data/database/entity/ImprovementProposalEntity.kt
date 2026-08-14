package com.aura.ai.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "improvement_proposals")
data class ImprovementProposalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val skillName: String?,
    val description: String,
    val proposalJson: String,
    val createdBy: String,
    val approved: Boolean = false,
    val approvedBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
