package com.aura.ai.ui.chat

/** Keeps chat availability aligned with the providers the reasoning router can actually call. */
internal object ChatBackendAvailabilityPolicy {
    data class Result(
        val ready: Boolean,
        val label: String,
        val banner: String? = null,
    )

    /** Keep chat controls available after history loads so Aura can explain a missing backend. */
    fun canSubmit(historyLoaded: Boolean): Boolean = historyLoaded

    fun evaluate(
        localModel: String?,
        cloudEnabled: Boolean,
        cloudBaseUrl: String,
        cloudModel: String,
        cloudApiKeyConfigured: Boolean,
    ): Result {
        val local = localModel?.trim().orEmpty()
        val cloud = cloudModel.trim()
        val cloudReady = cloudEnabled && cloudBaseUrl.isNotBlank() && cloud.isNotBlank() && cloudApiKeyConfigured
        if (cloudReady) {
            return Result(
                ready = true,
                label = if (local.isNotBlank()) "Cloud first · $cloud · Ollama fallback" else "Cloud · $cloud",
            )
        }
        if (local.isNotBlank()) return Result(true, "Ollama · $local")

        val cloudConfiguredButIncomplete = cloudEnabled &&
            (cloudBaseUrl.isBlank() || cloud.isBlank() || !cloudApiKeyConfigured)
        return Result(
            ready = false,
            label = "no model connected",
            banner = if (cloudConfiguredButIncomplete) {
                "Cloud is enabled but needs a server URL, model, and API key. Or connect Ollama in Settings."
            } else {
                "Connect Ollama, or enable cloud with a model and API key in Settings."
            },
        )
    }
}
