package com.aura.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aura.ai.data.local.entity.AgentActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentActionDao {
    @Insert
    suspend fun insertAction(action: AgentActionEntity): Long

    @Update
    suspend fun updateAction(action: AgentActionEntity)

    @Delete
    suspend fun deleteAction(action: AgentActionEntity)

    @Query("SELECT * FROM agent_action WHERE id = :id")
    suspend fun getActionById(id: Long): AgentActionEntity?

    @Query("SELECT * FROM agent_action WHERE requestId = :requestId")
    suspend fun getActionByRequestId(requestId: String): AgentActionEntity?

    @Query("SELECT * FROM agent_action WHERE status = :status ORDER BY createdTimestamp DESC LIMIT :limit")
    fun getActionsByStatus(status: String, limit: Int = 50): Flow<List<AgentActionEntity>>

    @Query("SELECT * FROM agent_action WHERE tool = :toolName ORDER BY createdTimestamp DESC LIMIT :limit")
    fun getActionsByTool(toolName: String, limit: Int = 50): Flow<List<AgentActionEntity>>

    @Query("SELECT * FROM agent_action ORDER BY createdTimestamp DESC LIMIT :limit")
    fun getRecentActions(limit: Int = 100): Flow<List<AgentActionEntity>>

    @Query("DELETE FROM agent_action WHERE completedTimestamp IS NOT NULL AND completedTimestamp < :beforeTimestamp")
    suspend fun deleteCompletedActionsOlderThan(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM agent_action")
    suspend fun getActionCount(): Int
}
