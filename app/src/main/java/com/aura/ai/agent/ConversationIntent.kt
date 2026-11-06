package com.aura.ai.agent

/** Keeps ordinary conversation to one model turn; device/work actions use the agent planner. */
internal object ConversationIntent {
    private val actionRequest = Regex(
        """^\s*(?:(?:please|can you|could you|would you|will you|can you help me|could you help me|i need you to|i want you to|i need to|i want to|help me|go ahead and)\s+)*(?:please[\s,]+)*(?:open|launch|close|call|dial|send|text|message|whatsapp|remind|schedule|create|delete|remove|install|update|change|switch|turn on|turn off|start|stop|search|browse|scan|diagnose|check|control|find|locate|build|code|edit|commit|push|deploy|merge|queue|play|mix|set)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val explicitAction = Regex(
        "\\b(?:make (?:a|the) (?:phone )?call|set (?:a )?reminder|remind me)\\b",
        RegexOption.IGNORE_CASE,
    )

    fun needsAgentPlan(request: String): Boolean {
        val command = com.aura.ai.voice.wake.WakePhraseMatcher.commandAfterWake(request) ?: request
        return actionRequest.containsMatchIn(command) || explicitAction.containsMatchIn(command)
    }

    fun needsSpokenAcknowledgement(request: String, spokenInstruction: Boolean): Boolean =
        spokenInstruction && needsAgentPlan(request)
}
