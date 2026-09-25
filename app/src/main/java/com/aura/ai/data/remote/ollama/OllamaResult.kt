package com.aura.ai.data.remote.ollama

/**
 * A typed outcome for every Ollama interaction. The agent and UI branch on this
 * instead of catching raw exceptions, so failures surface as actionable guidance
 * rather than stack traces.
 */
sealed interface OllamaResult<out T> {
    data class Success<T>(val value: T) : OllamaResult<T>
    data class Failure(val error: OllamaError) : OllamaResult<Nothing>
}

sealed class OllamaError(val message: String) {
    /** Host is set but nothing answered — wrong IP/port, server down, or firewall. */
    data class Unreachable(val host: String, val cause: String) :
        OllamaError("Can't reach Ollama at $host. Is `ollama serve` running and reachable? ($cause)")

    /** Connected, but the requested model isn't pulled on the server. */
    data class ModelNotFound(val model: String) :
        OllamaError("Model \"$model\" isn't installed on the Ollama host. Run `ollama pull $model`.")

    /** Server answered with a non-2xx status. */
    data class Http(val code: Int, val body: String) :
        OllamaError("Ollama returned HTTP $code: ${body.take(300)}")

    /** Request exceeded the configured timeout — common with big models on cold start. */
    data class Timeout(val seconds: Int) :
        OllamaError("Ollama timed out after ${seconds}s. Large models can be slow on first load; raise the timeout in Settings.")

    /** Malformed/unexpected payload. */
    data class Malformed(val detail: String) :
        OllamaError("Unexpected response from Ollama: $detail")

    /** No model has been selected yet. */
    data object NoModelSelected :
        OllamaError("No model selected. Pick a model in Settings first.")
}
