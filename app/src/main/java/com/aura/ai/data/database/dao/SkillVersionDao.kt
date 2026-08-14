package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.SkillVersionEntity

@Dao
interface SkillVersionDao {
    @Insert
    suspend fun insert(version: SkillVersionEntity)

    @Query("SELECT * FROM skill_versions WHERE skillName = :skillName ORDER BY createdAt DESC")
    suspend fun getHistory(skillName: String): List<SkillVersionEntity>
}
