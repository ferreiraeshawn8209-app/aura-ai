package com.aura.ai.agent.tool

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    tools: Set<@JvmSuppressWildcards Tool>,
) {
    private val byName: Map<String, Tool> = tools.associateBy { it.name }

    fun get(name: String): Tool? = byName[name]

    fun all(): Collection<Tool> = byName.values

    /** A compact catalog injected into the planning prompt. */
    fun catalog(): String = byName.values.joinToString("\n") { tool ->
        val params = if (tool.parameters.isEmpty()) {
            "no args"
        } else {
            tool.parameters.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        }
        "- ${tool.name}($params) — ${tool.description}"
    }
}
