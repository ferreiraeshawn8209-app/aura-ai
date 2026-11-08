package com.aura.ai.agent

/** Enforces that a required spoken acknowledgement completes before work begins. */
internal object SpokenActionGate {
    const val ACKNOWLEDGEMENT_TEXT = "I'll be back."

    suspend fun <T> run(
        requiresAcknowledgement: Boolean,
        acknowledge: suspend () -> Boolean,
        execute: suspend () -> T,
    ): T? {
        if (requiresAcknowledgement && !acknowledge()) return null
        return execute()
    }
}
