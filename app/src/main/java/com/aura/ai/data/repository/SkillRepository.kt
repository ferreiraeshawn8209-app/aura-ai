package com.aura.ai.data.repository

import com.aura.ai.data.local.dao.SkillDao
import com.aura.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SkillRepository @Inject constructor(
    private val skillDao: SkillDao
) {
    suspend fun createSkill(
        name: String,
        description: String,
        instructions: String,
        testCriteria: String,
        verificationCriteria: String
    ): Long {
        return skillDao.insertSkill(
            SkillEntity(
                name = name,
                description = description,
                instructions = instructions,
                status = "LEARN",
                testCriteria = testCriteria,
                verificationCriteria = verificationCriteria
            )
        )
    }

    suspend fun updateSkillStatus(id: Long, newStatus: String) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.updateSkill(
            skill.copy(
                status = newStatus,
                updatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordEvaluation(id: Long, evaluationData: String) {
        val skill = skillDao.getSkillById(id) ?: return
        skillDao.updateSkill(
            skill.copy(
                evaluationData = evaluationData,
                lastEvaluatedTimestamp = System.currentTimeMillis(),
                updatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun getSkillByName(name: String): SkillEntity? {
        return skillDao.getSkillByName(name)
    }

    fun getSkillsByStatus(status: String): Flow<List<SkillEntity>> {
        return skillDao.getSkillsByStatus(status)
    }

    fun getAllSkills(): Flow<List<SkillEntity>> {
        return skillDao.getAllSkills()
    }

    fun getTrustedSkills(): Flow<List<SkillEntity>> {
        return skillDao.getTrustedSkills()
    }

    suspend fun getSkillCount(): Int {
        return skillDao.getSkillCount()
    }
}
