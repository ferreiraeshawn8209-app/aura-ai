package com.aura.ai.agent.tool

/**
 * A capability the agent can invoke during EXECUTE. Every tool runs fully
 * on-device — there are no cloud calls other than the local Ollama inference.
 * Tools return a [ToolResult] so the agent observes real outcomes instead of
 * assuming success.
 */
interface Tool {
    val name: String
    val description: String

    /** Parameter name -> human description, injected into the planning prompt. */
    val parameters: Map<String, String>

    suspend fun execute(args: Map<String, String>): ToolResult
}

data class ToolResult(
    val success: Boolean,
    val output: String,
    val error: String? = null,
)
