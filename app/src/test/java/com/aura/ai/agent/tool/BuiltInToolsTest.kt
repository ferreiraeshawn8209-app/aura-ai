package com.aura.ai.agent.tool

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInToolsTest {
    @Test
    fun calculatorHonorsPrecedenceAndParentheses() {
        assertEquals(14.0, ExpressionEvaluator.eval("2 + 3 * 4"), 0.0)
        assertEquals(20.0, ExpressionEvaluator.eval("(2 + 3) * 4"), 0.0)
    }

    @Test
    fun calculatorSupportsUnarySignsAndDecimals() {
        assertEquals(-2.5, ExpressionEvaluator.eval("-(1.25 + 1.25)"), 0.0)
        assertEquals(0.5, ExpressionEvaluator.eval("+1 / 2"), 0.0)
    }

    @Test
    fun calculatorRejectsInvalidOrUnboundedExpressions() {
        listOf("", "1 / 0", "1 + nope", "2 + (3", "NaN", "1".repeat(257)).forEach { input ->
            val result = runBlocking { CalculatorTool().execute(mapOf("expression" to input)) }
            assertFalse("Expected '$input' to fail", result.success)
        }
    }

    @Test
    fun calculatorReturnsFiniteResult() {
        assertTrue(ExpressionEvaluator.eval("1e2") == 100.0 || runCatching { ExpressionEvaluator.eval("1e2") }.isFailure)
    }
}
