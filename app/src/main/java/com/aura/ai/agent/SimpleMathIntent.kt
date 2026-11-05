package com.aura.ai.agent

/** Deterministic handling for a small, common set of spoken arithmetic questions. */
internal object SimpleMathIntent {
    private val expression = Regex(
        "^\\s*(?:(?:what(?:'s| is)|calculate|compute)\\s+)?([\\w-]+)\\s+(plus|add|added to|minus|subtract(?:ed)? from|times|multiplied by|divided by|over)\\s+([\\w-]+)[?.!\\s]*$",
        RegexOption.IGNORE_CASE,
    )
    private val numbers = mapOf(
        "zero" to 0, "one" to 1, "two" to 2, "three" to 3, "four" to 4,
        "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9,
        "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13,
        "fourteen" to 14, "fifteen" to 15, "sixteen" to 16, "seventeen" to 17,
        "eighteen" to 18, "nineteen" to 19, "twenty" to 20,
    )

    fun answer(text: String): String? {
        val spokenRequest = com.aura.ai.voice.wake.WakePhraseMatcher.commandAfterWake(text) ?: text
        val match = expression.matchEntire(spokenRequest) ?: return null
        fun number(token: String): Int? = token.toIntOrNull()?.takeIf { it in 0..20 }
            ?: numbers[token.lowercase()]
        val left = number(match.groupValues[1]) ?: return null
        val right = number(match.groupValues[3]) ?: return null
        val value = when (match.groupValues[2].lowercase()) {
            "plus", "add", "added to" -> left + right
            "minus", "subtract", "subtracted", "subtract from" -> left - right
            "times", "multiplied by" -> left * right
            "divided by", "over" -> if (right != 0 && left % right == 0) left / right else return null
            else -> return null
        }
        return value.toString()
    }
}
