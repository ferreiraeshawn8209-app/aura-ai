package com.aura.ai.agent.core

import com.aura.ai.domain.model.AgentAction
import com.aura.ai.domain.model.AgentState
import com.aura.ai.domain.model.PermissionLevel
import com.aura.ai.domain.model.Tool
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor() {
    private val tools = mutableMapOf<String, Tool>()

    fun registerTool(tool: Tool) {
        tools[tool.name] = tool
    }

    fun getTool(name: String): Tool? = tools[name]

    fun getAllTools(): List<Tool> = tools.values.toList()

    fun getToolsByPermissionLevel(level: PermissionLevel): List<Tool> {
        return tools.values.filter { it.permissionLevel == level }
    }

    fun toolExists(name: String): Boolean = tools.containsKey(name)
}
