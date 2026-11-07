package com.aura.ai.ui.chat

/** Avoid executing a spoken device action when Android reports an uncertain transcript. */
internal object SpeechCommandConfidencePolicy {
    const val MIN_ACTION_CONFIDENCE = 0.45f

    fun shouldAskForRepeat(confidence: Float?, isActionRequest: Boolean): Boolean {
        if (!isActionRequest || confidence == null || confidence !in 0f..1f) return false
        return confidence < MIN_ACTION_CONFIDENCE
    }
}

/** Applies the low-confidence guard to the command tail returned by wake recognition. */
internal object WakeCommandConfidencePolicy {
    fun shouldAskForRepeat(command: String, confidence: Float?): Boolean =
        SpeechCommandConfidencePolicy.shouldAskForRepeat(
            confidence = confidence,
            isActionRequest = com.aura.ai.agent.ConversationIntent.needsAgentPlan(command),
        )
}

