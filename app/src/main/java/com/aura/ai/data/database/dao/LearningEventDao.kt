package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.LearningEventEntity

@Dao
interface LearningEventDao {
    @Insert
    suspend fun insert(event: LearningEventEntity)

    @Query("SELECT * FROM learning_events ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<LearningEventEntity>
}
