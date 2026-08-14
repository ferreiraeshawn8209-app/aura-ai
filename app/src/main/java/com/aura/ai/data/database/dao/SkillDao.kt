package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aura.ai.data.database.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(skill: SkillEntity)

    @Query("SELECT * FROM skills ORDER BY name ASC")
    fun getAll(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): SkillEntity?
}
