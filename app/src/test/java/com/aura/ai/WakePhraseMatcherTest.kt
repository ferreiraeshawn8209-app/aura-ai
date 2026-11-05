package com.aura.ai

import com.aura.ai.voice.wake.WakePhraseMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakePhraseMatcherTest {
    @Test
    fun recognizesOnlyYoYoYoAtStart() {
        assertTrue(WakePhraseMatcher.matches("Yo, yo, yo, what is five plus five?"))
        assertTrue(WakePhraseMatcher.matches("YO YO YO"))
        assertFalse(WakePhraseMatcher.matches("Hey Aura, what is five plus five?"))
        assertFalse(WakePhraseMatcher.matches("Please say yo, yo, yo"))
    }

    @Test
    fun extractsTheQuestionAfterWakePhrase() {
        assertEquals("what is five plus five?", WakePhraseMatcher.commandAfterWake("Yo, yo, yo, what is five plus five?"))
        assertEquals(null, WakePhraseMatcher.commandAfterWake("Yo, yo, yo"))
    }
}
