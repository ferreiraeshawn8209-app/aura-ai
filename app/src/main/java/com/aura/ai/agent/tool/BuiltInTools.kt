package com.aura.ai.agent.tool

import com.aura.ai.data.repository.SkillRepository
import com.aura.ai.data.repository.T1000MemoryRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Reads durable T1000 memories; this is intentionally separate from the legacy Aura key/value store. */
class RecallMemoryTool @Inject constructor(
    private val memory: T1000MemoryRepository,
) : Tool {
    override val name = "recall_memory"
    override val description = "Search long-term T1000 memories for facts, preferences, or prior context."
    override val parameters = mapOf("query" to "Text to find in saved memories")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val query = args["query"].orEmpty().trim()
        if (query.isBlank()) return ToolResult(false, "", "query is required")
        val hits = memory.recall(query)
        return if (hits.isEmpty()) {
            ToolResult(true, "No relevant memories found.")
        } else {
            ToolResult(true, hits.joinToString("\n") { "- (${it.type}) ${it.content}" })
        }
    }
}

/** Stores only facts the user explicitly asked the assistant to remember. */
class RememberTool @Inject constructor(
    private val memory: T1000MemoryRepository,
) : Tool {
    override val name = "remember"
    override val description = "Save a user-approved fact or preference to long-term memory. Use only when the user explicitly asks you to remember it."
    override val parameters = mapOf(
        "content" to "The fact the user explicitly asked to save",
        "importance" to "0.0 to 1.0 (optional; defaults to 0.6)",
    )

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val content = args["content"].orEmpty().trim()
        if (content.isBlank()) return ToolResult(false, "", "content is required")
        if (content.length > MAX_MEMORY_LENGTH) {
            return ToolResult(false, "", "content must be at most $MAX_MEMORY_LENGTH characters")
        }
        val importance = args["importance"]?.toFloatOrNull()?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.6f
        memory.remember(content = content, type = "FACT", importance = importance, source = "user-approved")
        return ToolResult(true, "Saved to long-term memory.")
    }

    private companion object {
        const val MAX_MEMORY_LENGTH = 2_000
    }
}

/** Looks up a previously learned procedure relevant to the task. */
class RecallSkillTool @Inject constructor(
    private val skills: SkillRepository,
) : Tool {
    override val name = "recall_skill"
    override val description = "Find a previously learned, reusable procedure relevant to the task."
    override val parameters = mapOf("query" to "Describe the task to find a matching skill")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val query = args["query"].orEmpty().trim()
        if (query.isBlank()) return ToolResult(false, "", "query is required")
        val found = skills.findRelevant(query)
        return if (found.isEmpty()) {
            ToolResult(true, "No matching skill yet.")
        } else {
            ToolResult(true, found.joinToString("\n") { "#${it.id} ${it.name} (v${it.version}): ${it.description}" })
        }
    }
}

/** Deterministic clock so time-sensitive answers aren't hallucinated. */
class DateTimeTool @Inject constructor() : Tool {
    override val name = "current_datetime"
    override val description = "Get the exact current local date and time."
    override val parameters = emptyMap<String, String>()

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val fmt = SimpleDateFormat("EEEE, d MMMM yyyy 'at' HH:mm:ss z", Locale.getDefault())
        return ToolResult(true, fmt.format(Date()))
    }
}

/** Safe arithmetic evaluator so math is computed, not guessed. */
class CalculatorTool @Inject constructor() : Tool {
    override val name = "calculator"
    override val description = "Evaluate an arithmetic expression (+ - * / parentheses, decimals)."
    override val parameters = mapOf("expression" to "e.g. (12.5 * 4) / 3")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val expression = args["expression"].orEmpty()
        return try {
            ToolResult(true, ExpressionEvaluator.eval(expression).toString())
        } catch (e: IllegalArgumentException) {
            ToolResult(false, "", "Could not evaluate expression: ${e.message}")
        }
    }
}

/** Tiny recursive-descent evaluator; it cannot execute arbitrary code. */
internal object ExpressionEvaluator {
    fun eval(input: String): Double {
        require(input.length in 1..256) { "expression must contain 1 to 256 characters" }
        val parser = Parser(input.filterNot(Char::isWhitespace))
        val result = parser.parseExpression()
        require(parser.atEnd()) { "unexpected trailing input" }
        require(result.isFinite()) { "result is not finite" }
        return result
    }

    private class Parser(private val source: String) {
        private var position = 0
        fun atEnd() = position >= source.length
        private fun peek() = source.getOrNull(position) ?: '\u0000'

        fun parseExpression(): Double {
            var value = parseTerm()
            while (peek() == '+' || peek() == '-') {
                val operator = source[position++]
                val rhs = parseTerm()
                value = if (operator == '+') value + rhs else value - rhs
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (peek() == '*' || peek() == '/') {
                val operator = source[position++]
                val rhs = parseFactor()
                if (operator == '/' && rhs == 0.0) throw IllegalArgumentException("division by zero")
                value = if (operator == '*') value * rhs else value / rhs
            }
            return value
        }

        private fun parseFactor(): Double {
            if (peek() == '(') {
                position++
                val value = parseExpression()
                if (peek() != ')') throw IllegalArgumentException("missing closing parenthesis")
                position++
                return value
            }
            if (peek() == '-') {
                position++
                return -parseFactor()
            }
            if (peek() == '+') {
                position++
                return parseFactor()
            }
            val start = position
            var decimalSeen = false
            while (position < source.length) {
                val char = source[position]
                if (char.isDigit()) {
                    position++
                } else if (char == '.' && !decimalSeen) {
                    decimalSeen = true
                    position++
                } else {
                    break
                }
            }
            if (start == position || source.substring(start, position) == ".") {
                throw IllegalArgumentException("expected a number")
            }
            return source.substring(start, position).toDoubleOrNull()
                ?: throw IllegalArgumentException("invalid number")
        }
    }
}
