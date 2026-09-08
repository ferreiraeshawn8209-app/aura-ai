package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.SandboxTestResultEntity

@Dao
interface SandboxTestResultDao {
    @Insert
    suspend fun insert(result: SandboxTestResultEntity)

    @Query("SELECT * FROM sandbox_test_results WHERE skillName = :skillName ORDER BY runAt DESC LIMIT :limit")
    suspend fun recentForSkill(skillName: String, limit: Int = 50): List<SandboxTestResultEntity>
}
