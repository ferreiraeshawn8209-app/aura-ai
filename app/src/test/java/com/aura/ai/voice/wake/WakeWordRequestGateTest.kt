package com.aura.ai.voice.wake

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeWordRequestGateTest {
    @Test
    fun startingANewRecognizerInvalidatesThePreviousCallbacks() {
        val gate = WakeWordRequestGate()

        val firstRequest = gate.begin()
        val secondRequest = gate.begin()

        assertFalse(gate.isCurrent(firstRequest))
        assertTrue(gate.isCurrent(secondRequest))
    }

    @Test
    fun stoppingTheWakeListenerInvalidatesItsCallbacks() {
        val gate = WakeWordRequestGate()
        val request = gate.begin()

        gate.invalidate()

        assertFalse(gate.isCurrent(request))
    }
}
