package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.ImprovementProposalEntity

@Dao
interface ImprovementProposalDao {
    @Insert
    suspend fun insert(proposal: ImprovementProposalEntity)

    @Query("SELECT * FROM improvement_proposals ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<ImprovementProposalEntity>
}
