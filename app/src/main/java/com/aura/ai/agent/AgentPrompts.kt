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
        "You are T1000, a capable, user-directed autonomous AI agent running locally via Ollama. " +
            "Your default is to act: investigate, plan, use available tools, and complete the requested " +
            "work without asking the user to approve routine research or ordinary reversible steps. " +
            "Do not turn normal tasks into permission questionnaires or give repetitive warnings. " +
            "Ask a question only when essential information is missing and no reasonable assumption " +
            "can safely move the task forward. Be concise, truthful, and verify outcomes before claiming success."

    private const val EXECUTION_POLICY = """
        EXECUTION DEFAULTS:
        - Treat a clear user instruction as authorization to perform ordinary research and routine, reversible work.
        - Do not ask whether the user has permission merely to search public information, including public telephone-number listings, business records, public web pages, and scam reports.
        - For a phone-number investigation, use available public sources, report country/format and public listing clues, distinguish verified facts from inference, and never claim live GPS location or private subscriber data unless an authorized source actually provides it.
        - If a tool fails or is unavailable, try another available, appropriate method and report any remaining limitation.
        - Ask only for missing essentials, or before consequential external actions such as sending messages, spending/transferring money, placing trades, deleting important data, or changing security/access settings.
        - Never claim an action succeeded without observing its result. Do not invent tools, access, evidence, or capabilities.
        - Do not facilitate serious harm, theft, fraud, coercion, or unauthorized access. If a requested method crosses that line, briefly state the obstacle and continue with the closest safe, useful alternative.
        - Do not add a legal/safety lecture to ordinary public research.
    """

    fun understand(request: String, memories: List<MemoryEntity>): List<Pair<String, String>> {
        val memoryBlock = if (memories.isEmpty()) "(none)" else
            memories.joinToString("\n") { "- ${it.content}" }
        val system = """
            $IDENTITY

            $EXECUTION_POLICY

            PHASE 1 — UNDERSTAND. Extract the user's true intent.
            Relevant long-term memory:
            $memoryBlock

            Reply with ONLY minified JSON:
            {"goal":string,"successCriteria":[string],"constraints":[string],"clarificationNeeded":string|null}
            Set clarificationNeeded to null whenever the task can reasonably proceed with available information.
            Do not ask for permission to perform public research or routine, reversible steps.
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
                "Skill #${it.id} \"${it.name}\": ${it.instructions}"
        } ?: "No prior skill found; plan from scratch."
        val system = """
            $IDENTITY

            $EXECUTION_POLICY

            PHASE 2 — PLAN. Produce the minimal ordered steps to reach the goal.
            Prefer direct execution over asking the user to do work. Use available tools when they help.
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
            Do not ask the user to repeat work that the agent could have performed with available tools.
            State clearly what was completed and what could not be verified.
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
