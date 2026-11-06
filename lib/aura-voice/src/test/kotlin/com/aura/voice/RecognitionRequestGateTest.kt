package com.aura.voice.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionRequestGateTest {
    @Test
    fun newerRequestInvalidatesCallbacksFromEarlierTurn() {
        val gate = RecognitionRequestGate()
        val first = gate.begin()
        val second = gate.begin()

        assertFalse(gate.isCurrent(first))
        assertTrue(gate.isCurrent(second))
    }

    @Test
    fun stoppingInvalidatesCurrentRequest() {
        val gate = RecognitionRequestGate()
        val active = gate.begin()

        gate.invalidate()

        assertFalse(gate.isCurrent(active))
    }
}
