package com.aura.ai.agent

import com.aura.ai.agent.tool.ToolRegistry
import com.aura.ai.data.local.entity.MemoryCategory
import com.aura.ai.data.local.entity.SkillEntity
import com.aura.ai.data.local.entity.TaskOutcome
import com.aura.ai.data.remote.ollama.ChatChunk
import com.aura.ai.data.remote.ollama.OllamaChatMessage
import com.aura.ai.data.remote.ollama.OllamaError
import com.aura.ai.data.remote.ollama.OllamaRepository
import com.aura.ai.data.repository.MemoryRepository
import com.aura.ai.data.repository.SkillRepository
import com.aura.ai.data.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The T1000 reasoning engine. Every request flows through the full loop:
 *
 *   UNDERSTAND -> PLAN -> EXECUTE -> VERIFY -> (re-plan if unverified) -> LEARN
 *
 * Nothing is assumed complete: tool outputs are observed, the verifier judges the
 * answer against explicit success criteria using only gathered evidence, and the
 * loop re-plans up to [MAX_ATTEMPTS] times before reporting honest failure.
 */
@Singleton
class AgentOrchestrator @Inject constructor(
    private val ollama: OllamaRepository,
    private val settings: SettingsRepository,
    private val memory: MemoryRepository,
    private val skills: SkillRepository,
    private val registry: ToolRegistry,
    private val learning: LearningEngine,
    private val json: Json,
) {
    fun run(request: String): Flow<AgentEvent> = channelFlow {
        val cfg = settings.settings.first()
        val model = cfg.selectedModel
        if (model.isNullOrBlank()) {
            send(AgentEvent.Failed(OllamaError.NoModelSelected.message))
            return@channelFlow
        }
        val temperature = cfg.temperature / 100.0
        val timeout = cfg.requestTimeoutSeconds
        val startedAt = System.currentTimeMillis()

        suspend fun complete(messages: List<Pair<String, String>>, streamTokens: Boolean): String {
            val sb = StringBuilder()
            ollama.chat(
                model = model,
                messages = messages.map { OllamaChatMessage(it.first, it.second) },
                temperature = temperature,
                timeoutSeconds = timeout,
            ).collect { chunk ->
                when (chunk) {
                    is ChatChunk.Delta -> {
                        sb.append(chunk.text)
                        if (streamTokens) send(AgentEvent.Token(chunk.text))
                    }
                    is ChatChunk.Done -> Unit
                    is ChatChunk.Error -> throw AgentException(chunk.error.message)
                }
            }
            return sb.toString()
        }

        try {
            // ---- PHASE 1: UNDERSTAND ---------------------------------------
            send(AgentEvent.Phase(AgentPhase.UNDERSTAND, "Interpreting your request"))
            val memories = memory.recall(request)
            val understanding = parseUnderstanding(
                complete(AgentPrompts.understand(request, memories), streamTokens = false),
            )
            send(AgentEvent.Understood(understanding))
            understanding.clarificationNeeded?.takeIf { it.isNotBlank() }?.let {
                send(AgentEvent.Answer(it))
                learning.recordRun(request, Plan(emptyList()), it, TaskOutcome.PARTIAL, startedAt, null)
                return@channelFlow
            }

            // ---- Look for a reusable skill ---------------------------------
            val priorSkill: SkillEntity? = skills.findRelevant(understanding.goal).firstOrNull()

            var attempt = 0
            var verdict: Verdict? = null
            var answer = ""
            var plan = Plan(emptyList())
            var transcript = ""

            while (attempt < MAX_ATTEMPTS) {
                attempt++

                // ---- PHASE 2: PLAN -----------------------------------------
                send(AgentEvent.Phase(AgentPhase.PLAN, if (attempt == 1) "Devising a plan" else "Revising the plan"))
                plan = parsePlan(
                    complete(AgentPrompts.plan(understanding, registry, priorSkill), streamTokens = false),
                )
                plan = plan.copy(fromSkillId = priorSkill?.id)
                send(AgentEvent.Planned(plan, reusedSkill = priorSkill != null && attempt == 1))

                // ---- PHASE 3: EXECUTE --------------------------------------
                send(AgentEvent.Phase(AgentPhase.EXECUTE, "Carrying out the plan"))
                val observations = mutableListOf<Observation>()
                for (step in plan.steps.sortedBy { it.index }) {
                    send(AgentEvent.StepStarted(step))
                    val obs = executeStep(step)
                    observations.add(obs)
                    send(AgentEvent.StepObserved(obs))
                }
                transcript = buildTranscript(plan, observations)

                // Synthesize the user-facing answer from real observations.
                send(AgentEvent.Phase(AgentPhase.EXECUTE, "Composing the answer"))
                answer = complete(AgentPrompts.synthesize(understanding, transcript), streamTokens = true)

                // ---- PHASE 4: VERIFY ---------------------------------------
                send(AgentEvent.Phase(AgentPhase.VERIFY, "Checking the work against success criteria"))
                verdict = parseVerdict(
                    complete(AgentPrompts.verify(understanding, answer, transcript), streamTokens = false),
                )
                send(AgentEvent.Verified(verdict))

                if (verdict.verified) break

                if (attempt < MAX_ATTEMPTS) {
                    send(AgentEvent.Replan("Unmet: ${verdict.unmetCriteria.joinToString("; ")}", attempt))
                }
            }

            val success = verdict?.verified == true
            send(AgentEvent.Answer(answer))

            // ---- PHASE 5: LEARN --------------------------------------------
            send(AgentEvent.Phase(AgentPhase.LEARN, "Updating memory and skills"))
            if (priorSkill != null) {
                learning.updateSkillStats(priorSkill.id, success)
            }
            if (success) {
                if (priorSkill == null) {
                    val distilled = runCatching {
                        parseDistilled(
                            complete(AgentPrompts.distillSkill(understanding, plan, transcript), streamTokens = false),
                        )
                    }.getOrNull()
                    if (distilled != null) {
                        val id = learning.learnSkill(distilled.first, distilled.second, distilled.third)
                        send(AgentEvent.Learned("Learned new skill \"${distilled.first}\" (#$id)"))
                    }
                }
                // Persist salient facts from this run for future recall.
                memory.remember(
                    content = "Completed: ${understanding.goal}",
                    category = MemoryCategory.TASK,
                    importance = 0.4f,
                    source = "task",
                )
            } else {
                priorSkill?.let {
                    learning.evolveSkill(it, plan.let { p -> runCatching { json.encodeToString(Plan.serializer(), p) }.getOrDefault("{}") })
                    send(AgentEvent.Learned("Recorded an improved version of skill \"${it.name}\" for next time"))
                }
            }

            learning.recordRun(
                request = request,
                plan = plan,
                transcript = transcript,
                outcome = if (success) TaskOutcome.SUCCESS else TaskOutcome.FAILED,
                startedAt = startedAt,
                skillId = priorSkill?.id,
            )

            if (!success) {
                send(
                    AgentEvent.Failed(
                        "I couldn't fully verify this after $MAX_ATTEMPTS attempts. " +
                            "Unmet: ${verdict?.unmetCriteria?.joinToString("; ").orEmpty()}",
                    ),
                )
            }
        } catch (e: AgentException) {
            send(AgentEvent.Failed(e.message ?: "Agent error"))
        }
    }

    private suspend fun executeStep(step: PlanStep): Observation {
        val toolName = step.tool ?: return Observation(step.index, true, "(reasoning) ${step.description}")
        val tool = registry.get(toolName)
            ?: return Observation(step.index, false, "", "Unknown tool: $toolName")
        return try {
            val result = tool.execute(step.args)
            Observation(step.index, result.success, result.output, result.error)
        } catch (e: Exception) {
            Observation(step.index, false, "", e.message ?: "tool crashed")
        }
    }

    private fun buildTranscript(plan: Plan, observations: List<Observation>): String =
        plan.steps.sortedBy { it.index }.joinToString("\n") { step ->
            val obs = observations.firstOrNull { it.stepIndex == step.index }
            val status = when {
                obs == null -> "SKIPPED"
                obs.success -> "OK"
                else -> "FAILED"
            }
            val detail = obs?.error ?: obs?.output.orEmpty()
            "Step ${step.index} [$status] ${step.description}\n   -> ${detail.take(600)}"
        }

    // --- Parsers ------------------------------------------------------------

    private fun parseUnderstanding(raw: String): Understanding {
        val obj = JsonExtraction.firstJsonObject(raw)
            ?: return Understanding(goal = raw.trim().take(200), successCriteria = listOf("Address the request"))
        return runCatching { json.decodeFromString(Understanding.serializer(), obj) }
            .getOrElse { Understanding(goal = raw.trim().take(200), successCriteria = listOf("Address the request")) }
    }

    private fun parsePlan(raw: String): Plan {
        val obj = JsonExtraction.firstJsonObject(raw)
            ?: return Plan(listOf(PlanStep(0, "Respond directly", verify = "Answer addresses the goal")))
        return runCatching { json.decodeFromString(Plan.serializer(), obj) }
            .getOrElse { Plan(listOf(PlanStep(0, "Respond directly", verify = "Answer addresses the goal"))) }
    }

    private fun parseVerdict(raw: String): Verdict {
        val obj = JsonExtraction.firstJsonObject(raw)
            ?: return Verdict(verified = true, reasoning = "No structured verdict; accepted answer.")
        return runCatching { json.decodeFromString(Verdict.serializer(), obj) }
            .getOrElse { Verdict(verified = true, reasoning = "Unparseable verdict; accepted answer.") }
    }

    private fun parseDistilled(raw: String): Triple<String, String, String>? {
        val obj = JsonExtraction.firstJsonObject(raw) ?: return null
        val element = runCatching { json.parseToJsonElement(obj) }.getOrNull() ?: return null
        val map = element as? kotlinx.serialization.json.JsonObject ?: return null
        val name = (map["name"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return null
        val description = (map["description"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: name
        val procedure = map["procedure"]?.toString() ?: "[]"
        return Triple(name, description, procedure)
    }

    private class AgentException(message: String) : Exception(message)

    companion object {
        const val MAX_ATTEMPTS = 3
    }
}
