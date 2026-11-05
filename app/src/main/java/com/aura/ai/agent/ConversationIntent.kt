package com.aura.ai.agent

/** Keeps ordinary conversation to one model turn; device/work actions use the agent planner. */
internal object ConversationIntent {
    private val taskLanguage = Regex(
        "\\b(open|launch|call|dial|send|text|message|whatsapp|remind|reminder|schedule|calendar|create|delete|remove|install|update|change|switch|turn on|turn off|start|stop|search|browse|scan|diagnose|check my (phone|computer|pc|device)|control|file|folder|build|code|edit|commit|push|deploy|merge|queue|play|mix)\\b",
        RegexOption.IGNORE_CASE,
    )

    fun needsAgentPlan(request: String): Boolean = taskLanguage.containsMatchIn(request)
}
