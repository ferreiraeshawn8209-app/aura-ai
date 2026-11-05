package com.aura.ai

import com.aura.ai.agent.SpokenActionGate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpokenActionGateTest {
    @Test
    fun acknowledgementFinishesBeforeActionStarts() = runBlocking {
        val order = mutableListOf<String>()
        val result = SpokenActionGate.run(
            requiresAcknowledgement = true,
            acknowledge = { order += "spoken"; true },
            execute = { order += "execute"; "done" },
        )
        assertEquals("done", result)
        assertEquals(listOf("spoken", "execute"), order)
    }

    @Test
    fun failedAcknowledgementPreventsAction() = runBlocking {
        var executed = false
        val result = SpokenActionGate.run(
            requiresAcknowledgement = true,
            acknowledge = { false },
            execute = { executed = true },
        )
        assertNull(result)
        assertEquals(false, executed)
    }

    @Test
    fun questionsSkipAcknowledgementAndRunDirectly() = runBlocking {
        val order = mutableListOf<String>()
        SpokenActionGate.run(
            requiresAcknowledgement = false,
            acknowledge = { order += "spoken"; true },
            execute = { order += "execute" },
        )
        assertEquals(listOf("execute"), order)
    }
}
