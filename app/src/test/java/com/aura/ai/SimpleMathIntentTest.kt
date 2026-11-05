package com.aura.ai

import com.aura.ai.agent.SimpleMathIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SimpleMathIntentTest {
    @Test
    fun answersTheSpokenFivePlusFiveQuestionDirectly() {
        assertEquals("10", SimpleMathIntent.answer("Yo, yo, yo, what's five plus five?"))
    }

    @Test
    fun supportsOtherBasicArithmeticWithoutGuessingUnknownInputs() {
        assertEquals("6", SimpleMathIntent.answer("what is two times three"))
        assertEquals("3", SimpleMathIntent.answer("calculate ten minus seven"))
        assertNull(SimpleMathIntent.answer("what time is it"))
    }
}
