package com.aura.ai.data.remote

import com.aura.ai.data.settings.T1000Settings

object OllamaConfiguration {
    const val connectionTimeoutSeconds = 15L
    const val readTimeoutSeconds = 90L
    const val writeTimeoutSeconds = 30L

    // Keep the legacy Retrofit provider aligned with the user-configurable
    // phone-to-PC endpoint and the model installed on the owner's Ollama host.
    val baseUrl: String get() = T1000Settings.DEFAULT_OLLAMA_URL.trimEnd('/') + "/"
    val model: String get() = T1000Settings.DEFAULT_MODEL
}
