package com.aura.ai.voice

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WakeWordEventsTest {
    @Test
    fun transcriptStreamPreservesExistingChatConsumerContract() = runBlocking {
        val events = WakeWordEvents()
        val transcript = events.events
        events.dispatch("Yo yo yo, open Settings", 0.81f, listOf("Yo yo yo open Settings"))

        assertEquals("Yo yo yo, open Settings", transcript.first())
    }

    @Test
    fun latestEventRetainsConfidenceAndAlternativeHypotheses() {
        val events = WakeWordEvents()
        events.dispatch("Yo yo yo, open Settings", 0.81f, listOf("Yo yo yo, open settings"))

        assertEquals(0.81f, events.latestEvent.value?.confidence)
        assertEquals(listOf("Yo yo yo, open settings"), events.latestEvent.value?.alternatives)
    }

    @Test
    fun dispatchWithoutOptionalMetadataDoesNotInventValues() {
        val events = WakeWordEvents()
        events.dispatch("Yo yo yo")

        assertNull(events.latestEvent.value?.confidence)
        assertEquals(emptyList<String>(), events.latestEvent.value?.alternatives)
    }
}
