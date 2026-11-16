package com.aura.ai.coding.domain

/**
 * Coordinates a bounded self-development cycle.
 *
 * The planner may propose code, but only the workspace can apply it. Every
 * proposed file change is validated and explicitly approved before it is
 * written. Verification is performed after the patch and a failed check causes
 * the workspace to restore its pre-change checkpoint.
 */
data class CodePatch(
    val path: String,
    val content: String,
    val reason: String,
)

data class CodePlan(
    val goal: String,
    val patches: List<CodePatch>,
    val acceptanceChecks: List<String>,
)

data class CodeCheckReport(
    val passed: Boolean,
    val summary: String,
    val failures: List<String> = emptyList(),
)

sealed interface SelfDevelopmentEvent {
    data class Planned(val plan: CodePlan) : SelfDevelopmentEvent
    data class AwaitingApproval(val patch: CodePatch) : SelfDevelopmentEvent
    data class PatchApplied(val path: String) : SelfDevelopmentEvent
    data class Verification(val report: CodeCheckReport) : SelfDevelopmentEvent
    data class Completed(val summary: String) : SelfDevelopmentEvent
    data class Rejected(val reason: String) : SelfDevelopmentEvent
    data class Failed(val reason: String) : SelfDevelopmentEvent
}

fun interface CodePlanner {
    suspend fun plan(request: String): Result<CodePlan>
}

interface CodeWorkspace {
    /** Create an isolated checkpoint/branch before applying any changes. */
    suspend fun createCheckpoint(): Result<String>

    /** Apply a single file patch to the isolated working branch. */
    suspend fun apply(patch: CodePatch): Result<Unit>

    /** Run the project's fixed, allow-listed build and test checks. */
    suspend fun verify(checks: List<String>): Result<CodeCheckReport>

    /** Restore the checkpoint if verification fails. */
    suspend fun restore(checkpoint: String): Result<Unit>
}

fun interface PatchApproval {
    suspend fun approve(patch: CodePatch): Boolean
}

class SelfDevelopmentAgent(
    private val planner: CodePlanner,
    private val workspace: CodeWorkspace,
    private val approval: PatchApproval,
    private val policy: CodeChangePolicy = CodeChangePolicy(),
) {
    suspend fun develop(request: String, emit: suspend (SelfDevelopmentEvent) -> Unit) {
        if (request.isBlank()) {
            emit(SelfDevelopmentEvent.Rejected("A coding objective is required."))
            return
        }

        val plan = planner.plan(request).getOrElse {
            emit(SelfDevelopmentEvent.Failed("Planning failed: ${it.message ?: "unknown error"}"))
            return
        }
        emit(SelfDevelopmentEvent.Planned(plan))

        if (plan.patches.isEmpty()) {
            emit(SelfDevelopmentEvent.Rejected("The plan contains no code changes."))
            return
        }

        val invalidPatch = plan.patches.firstOrNull { !policy.isAllowed(it.path) }
        if (invalidPatch != null) {
            emit(SelfDevelopmentEvent.Rejected("Protected or invalid path: ${invalidPatch.path}"))
            return
        }

        val checkpoint = workspace.createCheckpoint().getOrElse {
            emit(SelfDevelopmentEvent.Failed("Could not create a safe checkpoint: ${it.message}"))
            return
        }

        for (patch in plan.patches) {
            emit(SelfDevelopmentEvent.AwaitingApproval(patch))
            if (!approval.approve(patch)) {
                workspace.restore(checkpoint)
                emit(SelfDevelopmentEvent.Rejected("Change declined; the checkpoint was restored."))
                return
            }

            val applied = workspace.apply(patch)
            if (applied.isFailure) {
                workspace.restore(checkpoint)
                emit(SelfDevelopmentEvent.Failed("Could not apply ${patch.path}: ${applied.exceptionOrNull()?.message}"))
                return
            }
            emit(SelfDevelopmentEvent.PatchApplied(patch.path))
        }

        val report = workspace.verify(plan.acceptanceChecks).getOrElse {
            workspace.restore(checkpoint)
            emit(SelfDevelopmentEvent.Failed("Verification could not run: ${it.message}"))
            return
        }
        emit(SelfDevelopmentEvent.Verification(report))

        if (!report.passed) {
            val restored = workspace.restore(checkpoint)
            val suffix = if (restored.isSuccess) " The checkpoint was restored." else " WARNING: automatic restore failed."
            emit(SelfDevelopmentEvent.Failed(report.failures.joinToString().ifBlank { report.summary } + suffix))
            return
        }

        emit(SelfDevelopmentEvent.Completed(report.summary))
    }
}

/**
 * Initial deny-by-default guard for files that must never be edited by an
 * autonomous coding cycle. Paths are repository-relative.
 */
class CodeChangePolicy {
    fun isAllowed(path: String): Boolean {
        if (path.isBlank() || path.startsWith("/") || path.startsWith("\\\\")) return false
        if (path.any { it.isISOControl() }) return false

        val normalized = path.replace('\\\\', '/')
        val segments = normalized.split('/')
        if (segments.any { it.isBlank() || it == "." || it == ".." }) return false

        val lower = normalized.lowercase()
        val file = segments.last().lowercase()

        if (segments.any { it.equals(".git", true) || it.equals(".gradle", true) || it.equals("build", true) }) return false
        if (file == "local.properties" || file.startsWith(".env")) return false
        if (file.endsWith(".jks") || file.endsWith(".keystore") || file.endsWith(".p12")) return false
        if (file.contains("secret") || file.contains("credential") || file.contains("private_key")) return false
        if (lower.startsWith(".github/workflows/")) return false

        return true
    }
}
