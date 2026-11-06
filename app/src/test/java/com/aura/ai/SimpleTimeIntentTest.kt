package com.aura.ai

import com.aura.ai.agent.SimpleTimeIntent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SimpleTimeIntentTest {
    private val johannesburgClock = Clock.fixed(
        Instant.parse("2026-11-06T17:05:00Z"),
        ZoneId.of("Africa/Johannesburg"),
    )

    @Test
    fun answersLocalTimeWithWakePhrase() {
        assertEquals(
            "It's 7:05 PM.",
            SimpleTimeIntent.answer("Yo, yo, yo, what time is it?", johannesburgClock, Locale.US),
        )
    }

    @Test
    fun acceptsCommonLocalTimePhrasings() {
        assertEquals("It's 7:05 PM.", SimpleTimeIntent.answer("tell me the time please", johannesburgClock, Locale.US))
        assertEquals("It's 7:05 PM.", SimpleTimeIntent.answer("current time", johannesburgClock, Locale.US))
    }

    @Test
    fun doesNotGuessWhenAnotherLocationOrTimeContextIsRequested() {
        assertNull(SimpleTimeIntent.answer("what time is it in London", johannesburgClock, Locale.US))
        assertNull(SimpleTimeIntent.answer("what time is the meeting", johannesburgClock, Locale.US))
    }
}
