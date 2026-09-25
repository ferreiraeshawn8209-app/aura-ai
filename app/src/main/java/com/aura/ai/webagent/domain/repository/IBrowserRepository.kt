package com.aura.ai.webagent.domain.repository

import com.aura.ai.webagent.domain.BrowserAutomationRequest
import com.aura.ai.webagent.domain.BrowserAutomationResult

interface IBrowserRepository {
    suspend fun navigate(url: String): Result<String>
    suspend fun click(selector: String): Result<Boolean>
    suspend fun typeText(selector: String, text: String): Result<Boolean>
    suspend fun submitForm(formSelector: String): Result<Boolean>
    suspend fun getPageContent(): Result<String>
    suspend fun screenshot(): Result<ByteArray>
    suspend fun waitForElement(selector: String, timeoutMs: Long = 5000): Result<Boolean>
    suspend fun executeJavaScript(script: String): Result<String>
    suspend fun performAction(action: BrowserAutomationRequest): Result<BrowserAutomationResult>
    suspend fun close(): Result<Boolean>
}
