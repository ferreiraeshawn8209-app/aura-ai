package com.aura.ai.agent

/**
 * Local models frequently wrap JSON in prose or ```json fences. This pulls the
 * first balanced JSON object out of a completion so parsing stays robust without
 * forcing the model to be perfectly obedient.
 */
object JsonExtraction {
    fun firstJsonObject(raw: String): String? {
        val text = raw.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val start = text.indexOf('{')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until text.length) {
            val c = text[i]
            when {
                escaped -> escaped = false
                c == '\\' && inString -> escaped = true
                c == '"' -> inString = !inString
                !inString && c == '{' -> depth++
                !inString && c == '}' -> {
                    depth--
                    if (depth == 0) return text.substring(start, i + 1)
                }
            }
        }
        return null
    }
}
