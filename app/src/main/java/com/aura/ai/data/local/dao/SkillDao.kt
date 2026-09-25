package com.aura.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aura.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Insert
    suspend fun insertSkill(skill: SkillEntity): Long

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)

    @Query("SELECT * FROM skill WHERE id = :id")
    suspend fun getSkillById(id: Long): SkillEntity?

    @Query("SELECT * FROM skill WHERE name = :name")
    suspend fun getSkillByName(name: String): SkillEntity?

    @Query("SELECT * FROM skill WHERE status = :status ORDER BY updatedTimestamp DESC")
    fun getSkillsByStatus(status: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skill ORDER BY updatedTimestamp DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skill WHERE status = 'STORE' ORDER BY updatedTimestamp DESC")
    fun getTrustedSkills(): Flow<List<SkillEntity>>

    @Query("SELECT COUNT(*) FROM skill")
    suspend fun getSkillCount(): Int
}
