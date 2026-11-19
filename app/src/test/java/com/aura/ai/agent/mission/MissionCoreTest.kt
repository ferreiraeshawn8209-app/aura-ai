package com.aura.ai.agent.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionCoreTest {

    @Test
    fun charterPrioritizesIncomeBeforeLongevityAndSelfDevelopment() {
        assertEquals(
            listOf(
                MissionObjective.WEALTH,
                MissionObjective.LONGEVITY,
                MissionObjective.SELF_DEVELOPMENT
            ),
            MissionCore.objectives
        )
        assertEquals(1, MissionObjective.WEALTH.priority)
        assertEquals(2, MissionObjective.LONGEVITY.priority)
        assertEquals(3, MissionObjective.SELF_DEVELOPMENT.priority)
    }

    @Test
    fun validDailyMissionMustIncludeBothPrimaryObjectives() {
        val mission = DailyMission(
            date = "2026-11-20",
            tasks = listOf(
                MissionTask("income-1", MissionObjective.WEALTH, "Research a small-business automation offer"),
                MissionTask("longevity-1", MissionObjective.LONGEVITY, "Review new clinical trial evidence")
            ),
            reviewedAtEpochMillis = 1L
        )

        assertTrue(MissionCore.validateDailyMission(mission).isSuccess)
    }

    @Test
    fun rejectsDailyMissionMissingEitherPrimaryObjective() {
        val mission = DailyMission(
            date = "2026-11-20",
            tasks = listOf(
                MissionTask("build-1", MissionObjective.SELF_DEVELOPMENT, "Run unit tests")
            ),
            reviewedAtEpochMillis = 1L
        )

        assertFalse(MissionCore.validateDailyMission(mission).isSuccess)
    }

    @Test
    fun rejectsInvalidDateDuplicateIdsAndOversizedPlans() {
        val invalidDate = DailyMission(
            "20 November",
            listOf(
                MissionTask("income", MissionObjective.WEALTH, "Find a customer problem"),
                MissionTask("longevity", MissionObjective.LONGEVITY, "Review research")
            ),
            1L
        )
        assertFalse(MissionCore.validateDailyMission(invalidDate).isSuccess)

        val duplicateIds = DailyMission(
            "2026-11-20",
            listOf(
                MissionTask("same", MissionObjective.WEALTH, "Find a customer problem"),
                MissionTask("same", MissionObjective.LONGEVITY, "Review research")
            ),
            1L
        )
        assertFalse(MissionCore.validateDailyMission(duplicateIds).isSuccess)

        val tooMany = DailyMission(
            "2026-11-20",
            (1..21).map { index ->
                MissionTask("task-$index", if (index % 2 == 0) MissionObjective.WEALTH else MissionObjective.LONGEVITY, "Task $index")
            },
            1L
        )
        assertFalse(MissionCore.validateDailyMission(tooMany).isSuccess)
    }

    @Test
    fun prioritizationUsesCharterNotInputOrderAndPlacesIncompleteFirst() {
        val tasks = listOf(
            MissionTask("dev-done", MissionObjective.SELF_DEVELOPMENT, "Improve tests", completed = true),
            MissionTask("life", MissionObjective.LONGEVITY, "Review study"),
            MissionTask("wealth-done", MissionObjective.WEALTH, "Qualify lead", completed = true),
            MissionTask("wealth", MissionObjective.WEALTH, "Draft offer"),
        )

        assertEquals(
            listOf("wealth", "wealth-done", "life", "dev-done"),
            MissionCore.prioritize(tasks).map { it.id }
        )
    }
}
