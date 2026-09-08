package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.ai.data.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memory: MemoryEntity): Long

    @Query("SELECT * FROM memory ORDER BY timestamp DESC")
    fun getAll(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memory WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): MemoryEntity?

    @Query("DELETE FROM memory WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("SELECT * FROM memory ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<MemoryEntity>

    @Query("DELETE FROM memory")
    suspend fun clearAll()
}
