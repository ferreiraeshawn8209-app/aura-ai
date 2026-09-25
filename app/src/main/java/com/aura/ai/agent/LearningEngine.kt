package com.aura.ai.agent

import com.aura.ai.data.local.entity.SkillEntity
import com.aura.ai.data.local.entity.TaskHistoryEntity
import com.aura.ai.data.local.entity.TaskOutcome
import com.aura.ai.data.repository.SkillRepository
import com.aura.ai.data.local.dao.TaskHistoryDao
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Turns real, verified outcomes into durable improvements:
 *  - successful novel runs become new DRAFT skills, promoted to VERIFIED,
 *  - reused skills have their success rate updated from actual results,
 *  - failed skills that get fixed are stored as a new evolved version.
 *
 * This is genuine behavioral learning via accumulated procedures and statistics.
 * It does NOT fine-tune the Ollama model weights — that is out of scope for an
 * on-device app and we do not pretend otherwise.
 */
@Singleton
class LearningEngine @Inject constructor(
    private val skills: SkillRepository,
    private val taskHistory: TaskHistoryDao,
    private val json: Json,
) {
    suspend fun recordRun(
        request: String,
        plan: Plan,
        transcript: String,
        outcome: TaskOutcome,
        startedAt: Long,
        skillId: Long?,
    ) {
        taskHistory.insert(
            TaskHistoryEntity(
                request = request,
                planJson = runCatching { json.encodeToString(Plan.serializer(), plan) }.getOrDefault("{}"),
                outcomeJson = transcript.take(8000),
                outcome = outcome,
                skillId = skillId,
                startedAt = startedAt,
                finishedAt = System.currentTimeMillis(),
            ),
        )
    }

    /** Called when a reused skill produced a verified/failed outcome. */
    suspend fun updateSkillStats(skillId: Long, succeeded: Boolean) {
        skills.recordUse(skillId, succeeded)
    }

    /**
     * Persist a distilled procedure from a verified success as a new skill and
     * immediately mark it VERIFIED (it just passed real verification).
     */
    suspend fun learnSkill(name: String, description: String, procedureJson: String): Long {
        val id = skills.create(name, description, procedureJson)
        skills.markVerified(id)
        return id
    }

    /** Store a corrected version of a skill that previously failed. */
    suspend fun evolveSkill(parent: SkillEntity, newProcedureJson: String): Long =
        skills.evolve(parent, newProcedureJson)
}
