package com.aura.ai.domain.model

enum class AgentState {
    REQUESTED,
    PLANNED,
    AWAITING_PERMISSION,
    EXECUTING,
    VERIFYING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}

enum class PermissionLevel {
    LOW_RISK,
    CONFIRM_REQUIRED,
    HIGH_IMPACT
}

data class Tool(
    val name: String,
    val description: String,
    val parameters: Map<String, String>,
    val permissionLevel: PermissionLevel,
    val handler: suspend (Map<String, Any>) -> Result<String>,
    val verificationHandler: suspend (Map<String, Any>, String) -> Boolean
)

data class AgentAction(
    val id: String,
    val tool: Tool,
    val parameters: Map<String, Any>,
    var state: AgentState = AgentState.REQUESTED,
    var result: String? = null,
    var error: String? = null,
    val requiredPermission: PermissionLevel = PermissionLevel.LOW_RISK
)
