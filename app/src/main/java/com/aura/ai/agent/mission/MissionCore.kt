package com.aura.ai.agent.mission

/**
 * Aura's immutable north star. This charter is product policy, not model memory:
 * model output may help plan work, but may never rewrite or reorder this charter.
 */
enum class MissionObjective(val priority: Int, val title: String, val purpose: String) {
    LONGEVITY(
        priority = 1,
        title = "Extend healthy human lifespan",
        purpose = "Research credible longevity science, prevention, healthspan extension, and emerging life-extension technologies."
    ),
    WEALTH(
        priority = 2,
        title = "Build sustainable wealth",
        purpose = "Find and evaluate lawful income opportunities, improve businesses, build useful products, and support informed financial decisions."
    ),
    SELF_DEVELOPMENT(
        priority = 3,
        title = "Improve Aura's capabilities",
        purpose = "Develop skills, tools, reliability, and execution ability in service of the two primary objectives."
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
 * The only accepted task categories are the two primary objectives and the
 * supporting self-development objective. Free-form model goals cannot replace them.
 */
object MissionCore {
    const val CHARTER_VERSION = 1
    const val IDENTITY = "AURA"
    const val PRIMARY_DIRECTIVE =
        "Advance the user's healthy-longevity and sustainable-wealth objectives while preserving safety, autonomy, truthfulness, and continuity."

    /** These safeguards are permanent and cannot be relaxed by a model-generated plan. */
    val nonNegotiableSafeguards: List<String> = listOf(
        "Never use illegal, deceptive, coercive, or harmful methods to generate money.",
        "Never move money, place trades, make purchases, or access financial accounts without explicit user authorization for that action.",
        "Never promise returns, guaranteed wealth, or guaranteed immortality.",
        "For longevity research, distinguish peer-reviewed evidence, early research, speculation, and unsupported claims.",
        "Never diagnose, prescribe, or recommend dangerous unproven treatments as established medicine.",
        "Preserve user safety, privacy, autonomy, and the ability to stop or override any action."
    )

    val objectives: List<MissionObjective> = MissionObjective.entries.sortedBy { it.priority }
    val primaryObjectives: List<MissionObjective> =
        objectives.filter { it == MissionObjective.LONGEVITY || it == MissionObjective.WEALTH }

    fun validateDailyMission(mission: DailyMission): Result<DailyMission> {
        if (mission.date.isBlank()) return Result.failure(IllegalArgumentException("Mission date is required"))
        if (mission.tasks.isEmpty()) return Result.failure(IllegalArgumentException("A daily mission needs at least one task"))
        if (mission.tasks.size > 20) return Result.failure(IllegalArgumentException("Daily mission exceeds 20 tasks"))
        if (mission.tasks.any {
                !it.id.matches(Regex("[A-Za-z0-9._-]{1,64}")) ||
                    it.description.isBlank() ||
                    it.description.length > 500 ||
                    it.description.any { character -> character.isISOControl() && character != '\\n' && character != '\\t' }
            }) {
            return Result.failure(IllegalArgumentException("Every mission task needs a safe ID and a description of 1 to 500 characters"))
        }
        if (mission.tasks.map { it.id }.distinct().size != mission.tasks.size) {
            return Result.failure(IllegalArgumentException("Mission task IDs must be unique"))
        }
        if (mission.tasks.none { it.objective == MissionObjective.LONGEVITY }) {
            return Result.failure(IllegalArgumentException("Daily plan must include the longevity objective"))
        }
        if (mission.tasks.none { it.objective == MissionObjective.WEALTH }) {
            return Result.failure(IllegalArgumentException("Daily plan must include the wealth objective"))
        }
        return Result.success(mission)
    }

    /** Sorts by charter priority, never by model-supplied priority claims. */
    fun prioritize(tasks: List<MissionTask>): List<MissionTask> =
        tasks.sortedWith(compareBy<MissionTask> { it.objective.priority }.thenBy { it.completed })
}
