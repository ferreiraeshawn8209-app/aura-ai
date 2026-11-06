package com.aura.ai.agent.tool

import java.net.URI

/** Pure validation helpers shared by Android intent tools and JVM unit tests. */
internal object AndroidActionInputPolicy {
    fun normalizePhone(input: String): String? {
        val value = input.trim()
        if (value.any { !(it.isDigit() || it == '+' || it == ' ' || it == '-' || it == '(' || it == ')') }) {
            return null
        }
        val digits = value.filter(Char::isDigit)
        return digits.takeIf { it.length in 7..15 }
    }

    fun webUrl(input: String): String? {
        val value = input.trim().let { if (it.contains("://")) it else "https://$it" }
        return runCatching { URI(value) }.getOrNull()
            ?.takeIf { it.scheme.equals("http", true) || it.scheme.equals("https", true) }
            ?.takeIf { !it.host.isNullOrBlank() }
            ?.toASCIIString()
    }
}
