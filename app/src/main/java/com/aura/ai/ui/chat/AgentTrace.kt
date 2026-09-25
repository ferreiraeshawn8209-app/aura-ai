package com.aura.ai.ui.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.aura.ai.agent.AgentPhase
import com.aura.ai.ui.theme.AmberWarn
import com.aura.ai.ui.theme.MintAccent
import com.aura.ai.ui.theme.RoseError
import com.aura.ai.ui.theme.TextFaint

/**
 * The signature element of T1000: a compact, monospace telemetry log that shows
 * the agent reasoning through UNDERSTAND -> PLAN -> EXECUTE -> VERIFY -> LEARN in
 * real time. Everything else in the UI stays quiet so this reads clearly.
 */
@Composable
fun AgentTrace(lines: List<TraceLine>, modifier: Modifier = Modifier) {
    if (lines.isEmpty()) return
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        lines.forEach { line -> TraceRow(line) }
    }
}

@Composable
private fun TraceRow(line: TraceLine) {
    val dot = when (line.status) {
        TraceStatus.OK -> MintAccent
        TraceStatus.FAIL -> RoseError
        TraceStatus.INFO -> AmberWarn
        TraceStatus.ACTIVE -> MintAccent
    }
    val dotColor by animateColorAsState(dot, label = "dot")
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 3.dp),
    ) {
        Box(
            Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(if (line.status == TraceStatus.ACTIVE) dotColor.copy(alpha = 0.5f) else dotColor),
        )
        Text(
            text = phaseTag(line.phase),
            style = MaterialTheme.typography.labelSmall,
            color = TextFaint,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
        )
        Text(
            text = line.label,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = when (line.status) {
                TraceStatus.FAIL -> RoseError
                TraceStatus.INFO -> AmberWarn
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private fun phaseTag(phase: AgentPhase): String = when (phase) {
    AgentPhase.UNDERSTAND -> "UNDR"
    AgentPhase.PLAN -> "PLAN"
    AgentPhase.EXECUTE -> "EXEC"
    AgentPhase.VERIFY -> "VRFY"
    AgentPhase.LEARN -> "LERN"
}
