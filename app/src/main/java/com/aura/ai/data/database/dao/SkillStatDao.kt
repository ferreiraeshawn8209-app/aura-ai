package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.SkillStatEntity

@Dao
interface SkillStatDao {
    @Insert
    suspend fun insert(stat: SkillStatEntity)

    @Query("SELECT * FROM skill_stats WHERE skillName = :skillName ORDER BY lastObservedAt DESC")
    suspend fun getStatsForSkill(skillName: String): List<SkillStatEntity>
}
