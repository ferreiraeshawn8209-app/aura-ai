package com.aura.ai

import com.aura.ai.agent.LocalAnswerIntent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalAnswerIntentTest {
    @Test
    fun arithmeticRemainsAvailableThroughTheLocalFastPath() {
        assertEquals("10", LocalAnswerIntent.answer("Yo, yo, yo, what is five plus five?"))
    }

    @Test
    fun timeQuestionCanBeAnsweredWithNoModelDependency() {
        val clock = Clock.fixed(Instant.parse("2026-11-06T17:05:00Z"), ZoneId.of("Africa/Johannesburg"))
        assertEquals("It's 7:05 PM.", LocalAnswerIntent.answer("what time is it", clock, Locale.US))
    }
}
