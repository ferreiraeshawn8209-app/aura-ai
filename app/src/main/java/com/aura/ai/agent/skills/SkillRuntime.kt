package com.aura.ai.agent.skills

import com.aura.ai.agent.tool.ToolRegistry
import com.aura.ai.agent.tool.ToolResult

/**
 * Skills are declarative workflows, not executable source code.
 *
 * A model may propose a SkillManifest, but the runtime validates every tool
 * name and argument before execution. This deliberately avoids eval, scripts,
 * shell commands, and arbitrary downloaded code.
 */
data class SkillManifest(
    val name: String,
    val version: String,
    val purpose: String,
    val steps: List<SkillStep>,
    val enabled: Boolean = true,
)

data class SkillStep(
    val id: String,
    val toolName: String,
    val arguments: Map<String, String> = emptyMap(),
)

sealed interface SkillRunEvent {
    data class Started(val skillName: String, val version: String) : SkillRunEvent
    data class AwaitingApproval(val step: SkillStep) : SkillRunEvent
    data class StepSucceeded(val stepId: String, val output: String) : SkillRunEvent
    data class StepFailed(val stepId: String, val error: String) : SkillRunEvent
    data class Completed(val skillName: String) : SkillRunEvent
    data class Rejected(val reason: String) : SkillRunEvent
}

fun interface SkillApproval {
    suspend fun approve(skill: SkillManifest, step: SkillStep): Boolean
}

class SkillRuntime(
    private val tools: ToolRegistry,
    private val approval: SkillApproval,
    private val maxSteps: Int = 20,
) {
    suspend fun run(
        skill: SkillManifest,
        emit: suspend (SkillRunEvent) -> Unit = {},
    ): Result<List<ToolResult>> {
        val validationError = validate(skill)
        if (validationError != null) {
            emit(SkillRunEvent.Rejected(validationError))
            return Result.failure(IllegalArgumentException(validationError))
        }

        emit(SkillRunEvent.Started(skill.name, skill.version))
        val results = mutableListOf<ToolResult>()

        for (step in skill.steps) {
            val tool = tools.get(step.toolName)
                ?: return reject(emit, "Unknown tool: ${step.toolName}")

            val unknownArguments = step.arguments.keys - tool.parameters.keys
            if (unknownArguments.isNotEmpty()) {
                return reject(emit, "Unsupported arguments for ${step.toolName}: ${unknownArguments.joinToString()}")
            }

            // Approval is enforced by the runtime, never controlled by model-authored skill data.
            emit(SkillRunEvent.AwaitingApproval(step))
            if (!approval.approve(skill, step)) {
                return reject(emit, "Approval declined for step ${step.id}")
            }

            val result = try {
                tool.execute(step.arguments)
            } catch (error: Exception) {
                ToolResult(
                    success = false,
                    output = "",
                    error = error.message ?: "Tool execution failed",
                )
            }

            results += result
            if (!result.success) {
                val message = result.error ?: "Tool ${step.toolName} reported failure"
                emit(SkillRunEvent.StepFailed(step.id, message))
                return Result.failure(IllegalStateException(message))
            }

            emit(SkillRunEvent.StepSucceeded(step.id, result.output))
        }

        emit(SkillRunEvent.Completed(skill.name))
        return Result.success(results)
    }

    private fun validate(skill: SkillManifest): String? {
        if (!skill.enabled) return "Skill is disabled"
        if (!skill.name.matches(Regex("[a-zA-Z0-9][a-zA-Z0-9._-]{1,63}"))) return "Invalid skill name"
        if (skill.version.isBlank() || skill.version.length > 32) return "Invalid skill version"
        if (skill.purpose.isBlank() || skill.purpose.length > 500) return "Invalid skill purpose"
        if (skill.steps.isEmpty()) return "Skill must contain at least one step"
        if (skill.steps.size > maxSteps) return "Skill exceeds the $maxSteps step limit"
        if (skill.steps.map { it.id }.distinct().size != skill.steps.size) return "Step IDs must be unique"
        if (skill.steps.any { it.id.isBlank() || it.id.length > 64 }) return "Invalid step ID"
        if (skill.steps.any { it.toolName.isBlank() || it.toolName.length > 80 }) return "Invalid tool name"
        return null
    }

    private suspend fun reject(
        emit: suspend (SkillRunEvent) -> Unit,
        reason: String,
    ): Result<List<ToolResult>> {
        emit(SkillRunEvent.Rejected(reason))
        return Result.failure(IllegalArgumentException(reason))
    }
}
