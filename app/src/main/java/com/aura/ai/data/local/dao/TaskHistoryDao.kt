package com.aura.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.local.entity.TaskHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskHistoryDao {

    @Insert
    suspend fun insert(entry: TaskHistoryEntity): Long

    @Query("SELECT * FROM task_history ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<TaskHistoryEntity>>

    @Query("SELECT * FROM task_history WHERE request LIKE '%' || :query || '%' ORDER BY startedAt DESC LIMIT :limit")
    suspend fun similar(query: String, limit: Int = 5): List<TaskHistoryEntity>
}
