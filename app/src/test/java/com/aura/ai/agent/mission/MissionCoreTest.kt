package com.aura.ai.agent.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionCoreTest {
    private fun validMission() = DailyMission(
        date = "2026-11-17",
        reviewedAtEpochMillis = 1L,
        tasks = listOf(
            MissionTask("research", MissionObjective.LONGEVITY, "Review credible longevity research"),
            MissionTask("income", MissionObjective.WEALTH, "Identify a practical revenue opportunity"),
            MissionTask("skills", MissionObjective.SELF_DEVELOPMENT, "Improve one execution skill")
        )
    )

    @Test
    fun charterAlwaysKeepsLongevityAndWealthAsPrimaryObjectives() {
        assertEquals(
            listOf(MissionObjective.LONGEVITY, MissionObjective.WEALTH),
            MissionCore.primaryObjectives
        )
        assertEquals(1, MissionObjective.LONGEVITY.priority)
        assertEquals(2, MissionObjective.WEALTH.priority)
        assertEquals(3, MissionObjective.SELF_DEVELOPMENT.priority)
    }

    @Test
    fun dailyMissionMustAddressBothPrimaryObjectives() {
        assertTrue(MissionCore.validateDailyMission(validMission()).isSuccess)
        val missingWealth = validMission().copy(
            tasks = validMission().tasks.filterNot { it.objective == MissionObjective.WEALTH }
        )
        assertFalse(MissionCore.validateDailyMission(missingWealth).isSuccess)
    }

    @Test
    fun modelSuppliedTaskOrderCannotOverrideCharterPriority() {
        val reversed = validMission().tasks.reversed()
        val ordered = MissionCore.prioritize(reversed)
        assertEquals(MissionObjective.LONGEVITY, ordered.first().objective)
        assertEquals(MissionObjective.WEALTH, ordered[1].objective)
        assertEquals(MissionObjective.SELF_DEVELOPMENT, ordered[2].objective)
    }

    @Test
    fun duplicateTaskIdsAreRejected() {
        val mission = validMission().copy(
            tasks = validMission().tasks + MissionTask(
                "income", MissionObjective.WEALTH, "Duplicate ID"
            )
        )
        assertFalse(MissionCore.validateDailyMission(mission).isSuccess)
    }
}
