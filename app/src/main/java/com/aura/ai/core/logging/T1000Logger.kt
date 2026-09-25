package com.aura.ai.core.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class T1000Logger @Inject constructor() {
    fun d(tag: String, message: String) {
        Log.d("T1000-$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("T1000-$tag", message)
    }

    fun w(tag: String, message: String) {
        Log.w("T1000-$tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("T1000-$tag", message, throwable)
    }

    // Never log credentials or sensitive data
    fun logActionExecution(actionId: String, toolName: String, status: String) {
        i("AGENT", "Action $actionId ($toolName) -> $status")
    }

    fun logVoiceEvent(event: String) {
        i("VOICE", event)
    }

    fun logOllamaEvent(event: String) {
        i("OLLAMA", event)
    }
}
