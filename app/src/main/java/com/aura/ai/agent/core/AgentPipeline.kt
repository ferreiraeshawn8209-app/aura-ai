package com.aura.ai.agent.core

import com.aura.ai.data.repository.AgentRepository
import com.aura.ai.domain.model.AgentAction
import com.aura.ai.domain.model.AgentState
import com.aura.ai.domain.model.PermissionLevel
import com.aura.ai.domain.model.Tool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentPipeline @Inject constructor(
    private val toolRegistry: ToolRegistry,
    private val agentRepository: AgentRepository
) {
    private val _agentState = MutableStateFlow<AgentState>(AgentState.REQUESTED)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    suspend fun execute(
        toolName: String,
        parameters: Map<String, Any>,
        permissionCallback: suspend (PermissionLevel) -> Boolean = { true }
    ): Result<String> {
        val tool = toolRegistry.getTool(toolName)
            ?: return Result.failure(Exception("Tool '$toolName' not found"))

        return try {
            val requestId = agentRepository.recordAction(toolName, parameters.toString())
            _agentState.emit(AgentState.PLANNED)
            agentRepository.updateActionStatus(requestId, "PLANNED")

            if (tool.permissionLevel != PermissionLevel.LOW_RISK) {
                _agentState.emit(AgentState.AWAITING_PERMISSION)
                agentRepository.updateActionStatus(requestId, "AWAITING_PERMISSION")

                if (!permissionCallback(tool.permissionLevel)) {
                    _agentState.emit(AgentState.CANCELLED)
                    agentRepository.updateActionStatus(requestId, "CANCELLED")
                    return Result.failure(Exception("Permission denied"))
                }
            }

            _agentState.emit(AgentState.EXECUTING)
            agentRepository.updateActionStatus(requestId, "EXECUTING")

            val startTime = System.currentTimeMillis()
            val executionResult = tool.handler(parameters)
            val executionTimeMs = System.currentTimeMillis() - startTime

            val resultString = executionResult.getOrNull() ?: ""

            _agentState.emit(AgentState.VERIFYING)
            agentRepository.updateActionStatus(requestId, "VERIFYING")

            val verified = tool.verificationHandler(parameters, resultString)
            if (!verified) {
                _agentState.emit(AgentState.FAILED)
                agentRepository.recordActionError(requestId, "Verification failed", executionTimeMs)
                return Result.failure(Exception("Verification failed"))
            }

            _agentState.emit(AgentState.SUCCEEDED)
            agentRepository.recordActionResult(requestId, resultString, executionTimeMs)

            Result.success(resultString)
        } catch (e: Exception) {
            _agentState.emit(AgentState.FAILED)
            Result.failure(e)
        }
    }
}
