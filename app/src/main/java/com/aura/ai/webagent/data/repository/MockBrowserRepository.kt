package com.aura.ai.webagent.data.repository

import com.aura.ai.webagent.domain.BrowserAutomationRequest
import com.aura.ai.webagent.domain.BrowserAutomationResult
import com.aura.ai.webagent.domain.repository.IBrowserRepository
import javax.inject.Inject

class MockBrowserRepository @Inject constructor() : IBrowserRepository {
    override suspend fun navigate(url: String): Result<String> {
        return Result.failure(Exception("Browser automation requires external provider configuration. Legitimate authentication and CAPTCHA handling required."))
    }

    override suspend fun click(selector: String): Result<Boolean> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun typeText(selector: String, text: String): Result<Boolean> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun submitForm(formSelector: String): Result<Boolean> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun getPageContent(): Result<String> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun screenshot(): Result<ByteArray> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun waitForElement(selector: String, timeoutMs: Long): Result<Boolean> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun executeJavaScript(script: String): Result<String> {
        return Result.failure(Exception("Browser automation not configured"))
    }

    override suspend fun performAction(action: BrowserAutomationRequest): Result<BrowserAutomationResult> {
        return Result.success(
            BrowserAutomationResult(
                success = false,
                errorMessage = "Browser automation provider not configured. When configured, respects authentication, MFA, CAPTCHA, and rate limits."
            )
        )
    }

    override suspend fun close(): Result<Boolean> {
        return Result.success(true)
    }
}
