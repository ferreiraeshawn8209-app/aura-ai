package com.aura.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aura.ai.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("SELECT * FROM memory WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?

    @Query("SELECT * FROM memory WHERE type = :type ORDER BY importance DESC, updatedTimestamp DESC")
    fun getMemoriesByType(type: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory ORDER BY importance DESC, updatedTimestamp DESC LIMIT :limit")
    fun getTopMemories(limit: Int = 20): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory WHERE content LIKE '%' || :query || '%' ORDER BY importance DESC, updatedTimestamp DESC")
    fun searchMemories(query: String): Flow<List<MemoryEntity>>

    @Query("DELETE FROM memory WHERE createdTimestamp < :beforeTimestamp")
    suspend fun deleteMemoriesOlderThan(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM memory")
    suspend fun getMemoryCount(): Int
}
