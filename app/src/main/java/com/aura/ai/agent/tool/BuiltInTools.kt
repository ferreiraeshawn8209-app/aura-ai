package com.aura.ai.agent.tool

import com.aura.ai.data.local.entity.MemoryCategory
import com.aura.ai.data.repository.MemoryRepository
import com.aura.ai.data.repository.SkillRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/** Reads relevant long-term memories for the current request. */
class RecallMemoryTool @Inject constructor(
    private val memory: MemoryRepository,
) : Tool {
    override val name = "recall_memory"
    override val description = "Search the agent's long-term memory for facts, preferences, or prior context."
    override val parameters = mapOf("query" to "What to look up in memory")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val query = args["query"].orEmpty()
        if (query.isBlank()) return ToolResult(false, "", "query is required")
        val hits = memory.recall(query)
        return if (hits.isEmpty()) {
            ToolResult(true, "No relevant memories found.")
        } else {
            ToolResult(true, hits.joinToString("\n") { "- (${it.category}) ${it.content}" })
        }
    }
}

/** Persists a durable fact/preference the user shared. */
class RememberTool @Inject constructor(
    private val memory: MemoryRepository,
) : Tool {
    override val name = "remember"
    override val description = "Save a fact or preference to long-term memory for future sessions."
    override val parameters = mapOf(
        "content" to "The fact to remember",
        "importance" to "0.0 to 1.0",
    )

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val content = args["content"].orEmpty()
        if (content.isBlank()) return ToolResult(false, "", "content is required")
        val importance = args["importance"]?.toFloatOrNull()?.coerceIn(0f, 1f) ?: 0.6f
        memory.remember(content, MemoryCategory.FACT, importance, source = "agent")
        return ToolResult(true, "Saved to memory.")
    }
}

/** Looks up a previously learned procedure that may solve the task. */
class RecallSkillTool @Inject constructor(
    private val skills: SkillRepository,
) : Tool {
    override val name = "recall_skill"
    override val description = "Find a previously learned, reusable procedure relevant to the task."
    override val parameters = mapOf("query" to "Describe the task to find a matching skill")

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val query = args["query"].orEmpty()
        val found = skills.findRelevant(query)
        return if (found.isEmpty()) {
            ToolResult(true, "No matching skill yet.")
        } else {
            ToolResult(
                true,
                found.joinToString("\n") { "#${it.id} ${it.name} (v${it.version}, ${(it.successRate * 100).toInt()}% success): ${it.description}" },
            )
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
        val expr = args["expression"].orEmpty()
        return try {
            ToolResult(true, ExpressionEvaluator.eval(expr).toString())
        } catch (e: Exception) {
            ToolResult(false, "", "Could not evaluate \"$expr\": ${e.message}")
        }
    }
}

/**
 * Tiny recursive-descent evaluator. Intentionally supports only arithmetic so it
 * cannot execute arbitrary code — safe to run on untrusted model output.
 */
internal object ExpressionEvaluator {
    fun eval(input: String): Double {
        val tokens = input.replace(" ", "")
        val parser = Parser(tokens)
        val result = parser.parseExpression()
        if (!parser.atEnd()) throw IllegalArgumentException("Unexpected trailing input")
        return result
    }

    private class Parser(val s: String) {
        var pos = 0
        fun atEnd() = pos >= s.length
        private fun peek() = if (pos < s.length) s[pos] else '\u0000'

        fun parseExpression(): Double {
            var value = parseTerm()
            while (peek() == '+' || peek() == '-') {
                val op = s[pos++]
                val rhs = parseTerm()
                value = if (op == '+') value + rhs else value - rhs
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseFactor()
            while (peek() == '*' || peek() == '/') {
                val op = s[pos++]
                val rhs = parseFactor()
                value = if (op == '*') value * rhs else value / rhs
            }
            return value
        }

        private fun parseFactor(): Double {
            if (peek() == '(') {
                pos++
                val value = parseExpression()
                if (peek() != ')') throw IllegalArgumentException("Missing )")
                pos++
                return value
            }
            if (peek() == '-') { pos++; return -parseFactor() }
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            if (start == pos) throw IllegalArgumentException("Expected number")
            return s.substring(start, pos).toDouble()
        }
    }
}
