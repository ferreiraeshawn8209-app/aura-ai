package com.aura.ai.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CloudDailyQuotaTest {
    @Test
    fun countsOnlyRequestsWithinTheConfiguredDailyLimit() {
        assertEquals(1, SettingsRepository.CloudDailyQuota.nextCount("2026-11-06", "2026-11-06", 0, 2))
        assertEquals(2, SettingsRepository.CloudDailyQuota.nextCount("2026-11-06", "2026-11-06", 1, 2))
        assertNull(SettingsRepository.CloudDailyQuota.nextCount("2026-11-06", "2026-11-06", 2, 2))
    }

    @Test
    fun newUtcDayResetsUsageAndLimitIsClamped() {
        assertEquals(1, SettingsRepository.CloudDailyQuota.nextCount("2026-11-07", "2026-11-06", 100, 0))
        assertNull(SettingsRepository.CloudDailyQuota.nextCount("2026-11-06", "2026-11-06", 1000, 5000))
    }
}
