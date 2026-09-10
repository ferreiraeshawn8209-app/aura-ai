package com.aura.ai.network

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import com.aura.ai.BuildConfig

/**
 * Interceptor that scans request and response bodies and masks any occurrence of the
 * OpenAI API key string before it reaches the logging layer or is otherwise observed.
 * This is a defensive layer: it does not attempt to alter encrypted traffic but will
 * sanitize plain-text bodies that contain the key.
 */
class BodySanitizingInterceptor : Interceptor {
    private val apiKey = BuildConfig.OPENAI_API_KEY
    private val redaction = "[REDACTED_API_KEY]"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val sanitizedRequest = sanitizeRequestBody(originalRequest)

        val response = chain.proceed(sanitizedRequest)
        return sanitizeResponseBody(response)
    }

    private fun sanitizeRequestBody(request: Request): Request {
        val body = request.body ?: return request
        try {
            val buffer = Buffer()
            body.writeTo(buffer)
            var bodyString = buffer.readUtf8()
            if (apiKey.isNotEmpty() && bodyString.contains(apiKey)) {
                bodyString = bodyString.replace(apiKey, redaction)
                val mediaType: MediaType? = body.contentType()
                val newBody = RequestBody.create(mediaType, bodyString)
                // Build new request with sanitized body
                return request.newBuilder().method(request.method, newBody).build()
            }
        } catch (e: Exception) {
            // On error, return original request; do not fail the network call due to logging concerns
            return request
        }
        return request
    }

    private fun sanitizeResponseBody(response: Response): Response {
        val body = response.body ?: return response
        try {
            val source = body.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val buffer = source.buffer.clone()
            var bodyString = buffer.readUtf8()
            if (apiKey.isNotEmpty() && bodyString.contains(apiKey)) {
                bodyString = bodyString.replace(apiKey, redaction)
                val contentType = body.contentType()
                val newBody = ResponseBody.create(contentType, bodyString)
                return response.newBuilder().body(newBody).build()
            }
        } catch (e: Exception) {
            return response
        }
        return response
    }
}
