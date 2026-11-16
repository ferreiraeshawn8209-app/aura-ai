package com.aura.ai.agent.skills

import com.aura.ai.agent.tool.Tool
import com.aura.ai.agent.tool.ToolRegistry
import com.aura.ai.agent.tool.ToolResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillRuntimeTest {
    private class EchoTool : Tool {
        override val name = "echo"
        override val description = "Returns the supplied text"
        override val parameters = mapOf("text" to "Text to return")
        override suspend fun execute(args: Map<String, String>) =
            ToolResult(success = true, output = args["text"].orEmpty())
    }

    @Test
    fun runsRoutineDeclarativeSkillWithoutApproval() = runBlocking {
        val runtime = SkillRuntime(
            tools = ToolRegistry(setOf(EchoTool())),
            approval = SkillApproval { _, _ -> true },
        )
        val skill = SkillManifest(
            name = "hello-world",
            version = "1",
            purpose = "A small test workflow",
            steps = listOf(SkillStep("step-1", "echo", mapOf("text" to "hello"))),
        )

        val result = runtime.run(skill)

        assertTrue(result.isSuccess)
        assertEquals("hello", result.getOrThrow().single().output)
    }

    @Test
    fun stopsWhenApprovalIsDeclined() = runBlocking {
        var executed = false
        val tool = object : Tool {
            override val name = "dangerous"
            override val description = "Test tool"
            override val parameters = emptyMap<String, String>()
            override val requiresUserApproval = true
            override suspend fun execute(args: Map<String, String>): ToolResult {
                executed = true
                return ToolResult(true, "unexpected")
            }
        }
        val runtime = SkillRuntime(
            tools = ToolRegistry(setOf(tool)),
            approval = SkillApproval { _, _ -> false },
        )
        val skill = SkillManifest("approval-test", "1", "Approval test", listOf(SkillStep("one", "dangerous")))

        val result = runtime.run(skill)

        assertFalse(result.isSuccess)
        assertFalse(executed)
    }

    @Test
    fun rejectsUnknownToolArgumentsBeforeExecution() = runBlocking {
        val runtime = SkillRuntime(
            tools = ToolRegistry(setOf(EchoTool())),
            approval = SkillApproval { _, _ -> true },
        )
        val skill = SkillManifest(
            "bad-args",
            "1",
            "Unknown argument test",
            listOf(SkillStep("one", "echo", mapOf("shell" to "rm -rf /"))),
        )

        val result = runtime.run(skill)

        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Unsupported arguments"))
    }

    @Test
    fun rejectsOversizedSkills() = runBlocking {
        val runtime = SkillRuntime(
            tools = ToolRegistry(setOf(EchoTool())),
            approval = SkillApproval { _, _ -> true },
            maxSteps = 1,
        )
        val skill = SkillManifest(
            "too-many",
            "1",
            "Step limit test",
            listOf(
                SkillStep("one", "echo", mapOf("text" to "1")),
                SkillStep("two", "echo", mapOf("text" to "2")),
            ),
        )

        assertFalse(runtime.run(skill).isSuccess)
    }
}
