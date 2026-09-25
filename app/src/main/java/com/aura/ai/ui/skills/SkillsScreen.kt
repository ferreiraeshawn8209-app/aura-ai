package com.aura.ai.ui.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.data.local.entity.SkillEntity
import com.aura.ai.data.local.entity.SkillStatus
import com.aura.ai.ui.theme.AmberWarn
import com.aura.ai.ui.theme.MintAccent
import com.aura.ai.ui.theme.RoseError
import com.aura.ai.ui.theme.TextFaint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(viewModel: SkillsViewModel = hiltViewModel()) {
    val skills by viewModel.skills.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text("Learned skills", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Procedures T1000 distilled from verified successes",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextFaint,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        if (skills.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No skills yet. When T1000 completes and verifies a task, it distills a reusable procedure and it appears here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextFaint,
                    modifier = Modifier.padding(32.dp),
                )
            }
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(skills, key = { it.id }) { skill -> SkillCard(skill) }
            }
        }
    }
}

@Composable
private fun SkillCard(skill: SkillEntity) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(skill.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(skill.status)
        }
        Spacer(Modifier.height(6.dp))
        Text(skill.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Metric("v${skill.version}")
            Metric("${(skill.successRate * 100).toInt()}% success")
            Metric("used ${skill.timesUsed}×")
        }
    }
}

@Composable
private fun StatusChip(status: SkillStatus) {
    val (label, color) = when (status) {
        SkillStatus.VERIFIED -> "VERIFIED" to MintAccent
        SkillStatus.DRAFT -> "DRAFT" to AmberWarn
        SkillStatus.DEPRECATED -> "DEPRECATED" to RoseError
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun Metric(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
        color = TextFaint,
        modifier = Modifier.padding(end = 16.dp),
    )
}
