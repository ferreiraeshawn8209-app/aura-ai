package com.aura.ai.ui.chat

import com.aura.ai.agent.AgentPhase

/** A rendered chat bubble. Assistant turns carry a live trace of the agent loop. */
data class ChatTurn(
    val id: Long,
    val isUser: Boolean,
    val text: String,
    val trace: List<TraceLine> = emptyList(),
    val streaming: Boolean = false,
    val error: Boolean = false,
)

data class TraceLine(
    val phase: AgentPhase,
    val label: String,
    val status: TraceStatus = TraceStatus.ACTIVE,
)

enum class TraceStatus { ACTIVE, OK, FAIL, INFO }

data class ChatUiState(
    val turns: List<ChatTurn> = emptyList(),
    val input: String = "",
    val busy: Boolean = false,
    val listening: Boolean = false,
    val partialSpeech: String = "",
    val speaking: Boolean = false,
    val modelReady: Boolean = false,
    val modelName: String? = null,
    val banner: String? = null,
)
