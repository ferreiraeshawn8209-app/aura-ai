package com.aura.ai.data.repository

import com.aura.ai.data.local.dao.AgentActionDao
import com.aura.ai.data.local.entity.AgentActionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class AgentRepository @Inject constructor(
    private val agentActionDao: AgentActionDao
) {
    suspend fun recordAction(
        tool: String,
        parameters: String,
        status: String = "REQUESTED"
    ): String {
        val requestId = UUID.randomUUID().toString()
        agentActionDao.insertAction(
            AgentActionEntity(
                requestId = requestId,
                tool = tool,
                status = status,
                parameters = parameters
            )
        )
        return requestId
    }

    suspend fun updateActionStatus(requestId: String, newStatus: String) {
        val action = agentActionDao.getActionByRequestId(requestId) ?: return
        agentActionDao.updateAction(
            action.copy(
                status = newStatus,
                updatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordActionResult(
        requestId: String,
        result: String,
        executionTimeMs: Long
    ) {
        val action = agentActionDao.getActionByRequestId(requestId) ?: return
        agentActionDao.updateAction(
            action.copy(
                result = result,
                status = "SUCCEEDED",
                executionTimeMs = executionTimeMs,
                completedTimestamp = System.currentTimeMillis(),
                updatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordActionError(
        requestId: String,
        errorMessage: String,
        executionTimeMs: Long
    ) {
        val action = agentActionDao.getActionByRequestId(requestId) ?: return
        agentActionDao.updateAction(
            action.copy(
                errorMessage = errorMessage,
                status = "FAILED",
                executionTimeMs = executionTimeMs,
                completedTimestamp = System.currentTimeMillis(),
                updatedTimestamp = System.currentTimeMillis()
            )
        )
    }

    fun getRecentActions(limit: Int = 100): Flow<List<AgentActionEntity>> {
        return agentActionDao.getRecentActions(limit)
    }

    fun getActionsByStatus(status: String): Flow<List<AgentActionEntity>> {
        return agentActionDao.getActionsByStatus(status)
    }

    suspend fun getActionCount(): Int {
        return agentActionDao.getActionCount()
    }
}
