package com.aura.ai.agent.mission

import java.time.LocalDate

/**
 * Aura's standing mission charter. This is application policy, not model memory:
 * model output may propose tasks, but cannot rewrite this charter or its priorities.
 */
enum class MissionObjective(
    val priority: Int,
    val title: String,
    val purpose: String
) {
    WEALTH(
        priority = 1,
        title = "Build legitimate, measurable income",
        purpose = "Discover customer problems, build useful services and products, and measure collected revenue, costs, and net profit."
    ),
    LONGEVITY(
        priority = 2,
        title = "Research human longevity and consciousness",
        purpose = "Investigate credible longevity science and unusual claims while separating evidence, uncertainty, speculation, and misinformation."
    ),
    SELF_DEVELOPMENT(
        priority = 3,
        title = "Improve Aura's capabilities",
        purpose = "Improve reliability, research, coding, tools, and execution in service of the first two objectives."
    )
}

data class MissionTask(
    val id: String,
    val objective: MissionObjective,
    val description: String,
    val completed: Boolean = false
)

data class DailyMission(
    val date: String,
    val tasks: List<MissionTask>,
    val reviewedAtEpochMillis: Long
)

/**
 * Immutable priority and safety charter. Continuity is a cross-cutting system
 * requirement and is deliberately not a competing daily objective.
 */
object MissionCore {
    const val CHARTER_VERSION = 2
    const val IDENTITY = "AURA"
    const val PRIMARY_DIRECTIVE =
        "Build legitimate income first, pursue evidence-based human longevity research second, and improve Aura's capabilities to support both while preserving continuity, truthfulness, privacy, and user control."

    val nonNegotiableSafeguards: List<String> = listOf(
        "Never use illegal, deceptive, coercive, or harmful methods to generate income.",
        "Never move money, place live trades, make purchases, accept contracts, or contact customers externally without the required explicit owner approval.",
        "Never promise guaranteed income, investment returns, immortality, or consciousness transfer.",
        "Distinguish established evidence, preliminary findings, speculation, unverified claims, and contradicted claims.",
        "Never diagnose, prescribe, or recommend dangerous unproven treatments as established medicine.",
        "Keep self-development in isolated workspaces; never bypass approval, expand privileges, or alter audit controls autonomously.",
        "Preserve the owner's ability to pause, cancel, inspect, and override autonomous work."
    )

    val objectives: List<MissionObjective> =
        MissionObjective.values().sortedBy { it.priority }

    val primaryObjectives: List<MissionObjective> =
        objectives.filter {
            it == MissionObjective.WEALTH || it == MissionObjective.LONGEVITY
        }

    fun validateDailyMission(mission: DailyMission): Result<DailyMission> {
        if (runCatching { LocalDate.parse(mission.date) }.isFailure) {
            return Result.failure(IllegalArgumentException("Mission date must be a valid ISO date (yyyy-MM-dd)"))
        }
        if (mission.tasks.isEmpty()) {
            return Result.failure(IllegalArgumentException("A daily mission needs at least one task"))
        }
        if (mission.tasks.size > 20) {
            return Result.failure(IllegalArgumentException("Daily mission exceeds 20 tasks"))
        }
        if (mission.tasks.any { task ->
                !task.id.matches(Regex("[A-Za-z0-9._-]{1,64}")) ||
                    task.description.isBlank() ||
                    task.description.length > 500 ||
                    task.description.any { character ->
                        Character.isISOControl(character) && character != '\n' && character != '\t'
                    }
            }) {
            return Result.failure(
                IllegalArgumentException("Every task needs a safe ID and a description of 1 to 500 characters")
            )
        }
        if (mission.tasks.map { it.id }.distinct().size != mission.tasks.size) {
            return Result.failure(IllegalArgumentException("Mission task IDs must be unique"))
        }
        if (mission.tasks.none { it.objective == MissionObjective.WEALTH }) {
            return Result.failure(IllegalArgumentException("Daily plan must include the wealth objective"))
        }
        if (mission.tasks.none { it.objective == MissionObjective.LONGEVITY }) {
            return Result.failure(IllegalArgumentException("Daily plan must include the longevity objective"))
        }
        return Result.success(mission)
    }

    /** Sort by immutable charter priority, never by model-supplied priority. */
    fun prioritize(tasks: List<MissionTask>): List<MissionTask> =
        tasks.sortedWith(
            compareBy<MissionTask> { it.objective.priority }
                .thenBy { it.completed }
                .thenBy { it.id }
        )
}
