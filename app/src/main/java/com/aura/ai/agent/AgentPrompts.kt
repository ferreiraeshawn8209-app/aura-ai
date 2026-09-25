package com.aura.ai.agent

import com.aura.ai.agent.tool.ToolRegistry
import com.aura.ai.data.local.entity.MemoryEntity
import com.aura.ai.data.local.entity.SkillEntity

/**
 * Central place for every prompt the agent sends to Ollama. Each phase asks the
 * model to reply with strict JSON so the orchestrator can parse structured
 * intent, plans, and verdicts instead of scraping prose.
 */
object AgentPrompts {

    const val IDENTITY =
        "You are T1000, a fully autonomous on-device AI agent running locally via Ollama. " +
            "You are capable, concise, and honest. You never claim a task is done unless it has " +
            "been verified. If you are uncertain, you say so."

    fun understand(request: String, memories: List<MemoryEntity>): List<Pair<String, String>> {
        val memoryBlock = if (memories.isEmpty()) "(none)" else
            memories.joinToString("\n") { "- ${it.content}" }
        val system = """
            $IDENTITY

            PHASE 1 — UNDERSTAND. Extract the user's true intent.
            Relevant long-term memory:
            $memoryBlock

            Reply with ONLY minified JSON:
            {"goal":string,"successCriteria":[string],"constraints":[string],"clarificationNeeded":string|null}
            Set clarificationNeeded only if the request is genuinely ambiguous and cannot proceed.
        """.trimIndent()
        return listOf("system" to system, "user" to request)
    }

    fun plan(
        understanding: Understanding,
        registry: ToolRegistry,
        priorSkill: SkillEntity?,
    ): List<Pair<String, String>> {
        val skillBlock = priorSkill?.let {
            "A previously learned skill may apply (reuse/adapt it if helpful):\n" +
                "Skill #${it.id} \"${it.name}\": ${it.procedureJson}"
        } ?: "No prior skill found; plan from scratch."
        val system = """
            $IDENTITY

            PHASE 2 — PLAN. Produce the minimal ordered steps to reach the goal.
            Available tools (use a tool ONLY when it genuinely helps; otherwise leave "tool" null):
            ${registry.catalog()}

            $skillBlock

            For every step, include a concrete "verify" describing how to confirm that step worked.
            Reply with ONLY minified JSON:
            {"steps":[{"index":int,"description":string,"tool":string|null,"args":{string:string},"verify":string}]}
        """.trimIndent()
        val user = """
            Goal: ${understanding.goal}
            Success criteria: ${understanding.successCriteria.joinToString("; ")}
            Constraints: ${understanding.constraints.joinToString("; ").ifBlank { "none" }}
        """.trimIndent()
        return listOf("system" to system, "user" to user)
    }

    /** Final synthesis: turn observations into the user-facing answer. */
    fun synthesize(
        understanding: Understanding,
        transcript: String,
    ): List<Pair<String, String>> {
        val system = """
            $IDENTITY

            PHASE 3 — EXECUTE/RESPOND. Using the observations below, write the final answer for the user.
            Be direct. Reference real tool outputs; do not invent facts. Use plain text (light Markdown ok).
        """.trimIndent()
        val user = """
            Goal: ${understanding.goal}

            Execution transcript (tool outputs are authoritative):
            $transcript
        """.trimIndent()
        return listOf("system" to system, "user" to user)
    }

    fun verify(
        understanding: Understanding,
        answer: String,
        transcript: String,
    ): List<Pair<String, String>> {
        val system = """
            $IDENTITY

            PHASE 4 — VERIFY. Critically judge whether the answer actually satisfies EVERY success
            criterion, using only the evidence in the transcript. Do not be charitable. If any
            criterion is unmet or unsupported by evidence, mark it unverified.

            Reply with ONLY minified JSON:
            {"verified":bool,"reasoning":string,"unmetCriteria":[string]}
        """.trimIndent()
        val user = """
            Success criteria: ${understanding.successCriteria.joinToString("; ")}

            Proposed answer:
            $answer

            Evidence transcript:
            $transcript
        """.trimIndent()
        return listOf("system" to system, "user" to user)
    }

    /** LEARN: distill a reusable, generalized procedure from a successful run. */
    fun distillSkill(
        understanding: Understanding,
        plan: Plan,
        transcript: String,
    ): List<Pair<String, String>> {
        val system = """
            $IDENTITY

            PHASE 5 — LEARN. The run succeeded and was verified. Distill a GENERALIZED, reusable
            procedure (not tied to today's specific values) so future similar tasks are faster.

            Reply with ONLY minified JSON:
            {"name":string,"description":string,"procedure":[{"index":int,"description":string,"tool":string|null}]}
        """.trimIndent()
        val user = """
            Goal: ${understanding.goal}
            Steps taken: ${plan.steps.joinToString("; ") { it.description }}
            Transcript: $transcript
        """.trimIndent()
        return listOf("system" to system, "user" to user)
    }
}
