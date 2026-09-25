package com.aura.ai.webagent.domain

import kotlinx.serialization.Serializable

enum class BrowserAutomationAction {
    NAVIGATE,
    CLICK,
    TYPE_TEXT,
    SUBMIT_FORM,
    SCREENSHOT,
    GET_TEXT,
    WAIT_FOR_ELEMENT,
    SCROLL,
    EXECUTE_JAVASCRIPT
}

@Serializable
data class BrowserAutomationRequest(
    val action: String, // NAVIGATE, CLICK, TYPE_TEXT, etc.
    val url: String? = null,
    val selector: String? = null,
    val text: String? = null,
    val javascript: String? = null,
    val waitTimeMs: Long = 5000
)

@Serializable
data class BrowserAutomationResult(
    val success: Boolean,
    val content: String? = null,
    val errorMessage: String? = null,
    val captchaDetected: Boolean = false,
    val authenticationRequired: Boolean = false
)

data class WebAgentState {
    val currentUrl: String? = null
    val isAuthenticated: Boolean = false
    val requiresMfa: Boolean = false
    val requiresCaptcha: Boolean = false
    val lastError: String? = null
}
