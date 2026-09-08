package com.aura.ai

import okhttp3.logging.HttpLoggingInterceptor

/**
 * Test logger that stores logs in-memory so tests can assert redaction.
 */
class TestHttpLogger : HttpLoggingInterceptor.Logger {
    val lines = mutableListOf<String>()
    override fun log(message: String) {
        synchronized(lines) { lines.add(message) }
    }
}
