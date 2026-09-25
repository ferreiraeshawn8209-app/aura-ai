package com.aura.ai.agent

import kotlinx.serialization.Serializable

/** The agent's structured understanding of what the user actually wants. */
@Serializable
data class Understanding(
    val goal: String,
    val successCriteria: List<String>,
    val constraints: List<String> = emptyList(),
    val clarificationNeeded: String? = null,
)

/** A single actionable step. Either a tool invocation or a reasoning/synthesis step. */
@Serializable
data class PlanStep(
    val index: Int,
    val description: String,
    val tool: String? = null,
    val args: Map<String, String> = emptyMap(),
    /** How we will know THIS step worked — used by the verify phase. */
    val verify: String? = null,
)

@Serializable
data class Plan(
    val steps: List<PlanStep>,
    /** Id of a reused skill, if this plan came from one. */
    val fromSkillId: Long? = null,
)

/** The observed result of executing a step. Never assumed — always recorded. */
@Serializable
data class Observation(
    val stepIndex: Int,
    val success: Boolean,
    val output: String,
    val error: String? = null,
)

/** The verifier's judgment of the whole run against the success criteria. */
@Serializable
data class Verdict(
    val verified: Boolean,
    val reasoning: String,
    val unmetCriteria: List<String> = emptyList(),
)

/**
 * Streamed to the UI so the user watches the agent think, act, and check its
 * own work in real time rather than staring at a spinner.
 */
sealed interface AgentEvent {
    data class Phase(val phase: AgentPhase, val detail: String) : AgentEvent
    data class Understood(val understanding: Understanding) : AgentEvent
    data class Planned(val plan: Plan, val reusedSkill: Boolean) : AgentEvent
    data class StepStarted(val step: PlanStep) : AgentEvent
    data class StepObserved(val observation: Observation) : AgentEvent
    data class Token(val text: String) : AgentEvent
    data class Verified(val verdict: Verdict) : AgentEvent
    data class Learned(val note: String) : AgentEvent
    data class Replan(val reason: String, val attempt: Int) : AgentEvent
    data class Answer(val text: String) : AgentEvent
    data class Failed(val message: String) : AgentEvent
}

enum class AgentPhase { UNDERSTAND, PLAN, EXECUTE, VERIFY, LEARN }
