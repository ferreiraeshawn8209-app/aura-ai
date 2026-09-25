package com.aura.ai.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.aura.ai.data.remote.ollama.OllamaModel
import com.aura.ai.ui.theme.MintAccent
import com.aura.ai.ui.theme.RoseError
import com.aura.ai.ui.theme.TextFaint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val conn by viewModel.conn.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Section("Ollama connection") {
                    Text(
                        "T1000 runs entirely on your own Ollama server — no cloud. Point it at the host running `ollama serve`. " +
                            "Use 10.0.2.2 for an emulator reaching your computer, or the LAN IP of the machine on a real device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = conn.urlDraft,
                        onValueChange = viewModel::onUrlDraftChange,
                        label = { Text("Base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = viewModel::saveUrlAndTest, enabled = !conn.testing) {
                            Text(if (conn.testing) "Testing…" else "Connect & test")
                        }
                        Spacer(Modifier.size(12.dp))
                        when (conn.connected) {
                            true -> StatusPill("ONLINE", MintAccent)
                            false -> StatusPill("OFFLINE", RoseError)
                            null -> Unit
                        }
                    }
                    conn.message?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (conn.connected == false) RoseError else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Section("Model") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Installed models",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(onClick = viewModel::discoverModels, enabled = !conn.discovering) {
                            Text(if (conn.discovering) "Scanning…" else "Refresh")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (conn.models.isEmpty()) {
                        Text(
                            "No models discovered yet. Connect above, then pull one on the host, e.g. `ollama pull llama3.2`.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextFaint,
                        )
                    } else {
                        conn.models.forEach { model ->
                            ModelRow(
                                model = model,
                                selected = model.name == settings.selectedModel,
                                onClick = { viewModel.selectModel(model.name) },
                            )
                        }
                    }
                }
            }

            item {
                Section("Inference") {
                    LabeledSlider(
                        label = "Temperature",
                        value = settings.temperature,
                        valueLabel = "%.2f".format(settings.temperature / 100.0),
                        range = 0..100,
                        onChange = viewModel::setTemperature,
                    )
                    Spacer(Modifier.height(16.dp))
                    LabeledSlider(
                        label = "Request timeout",
                        value = settings.requestTimeoutSeconds,
                        valueLabel = "${settings.requestTimeoutSeconds}s",
                        range = 10..600,
                        onChange = viewModel::setTimeout,
                    )
                }
            }

            item {
                Section("Behavior") {
                    ToggleRow("Autonomous mode", "Let T1000 plan and act without step-by-step confirmation.", settings.autonomyEnabled, viewModel::setAutonomy)
                    ToggleRow("Spoken replies", "Read answers aloud with text-to-speech.", settings.voiceRepliesEnabled, viewModel::setVoiceReplies)
                    ToggleRow("\"Hey T1000\" wake phrase", "Keep a foreground listener running for the wake phrase.", settings.wakeWordEnabled, viewModel::setWakeWord)
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MintAccent)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun StatusPill(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun ModelRow(model: OllamaModel, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(model.name, style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace))
            val meta = listOfNotNull(
                model.details?.parameterSize,
                model.details?.quantizationLevel,
                if (model.size > 0) "%.1f GB".format(model.size / 1e9) else null,
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(meta, style = MaterialTheme.typography.labelMedium, color = TextFaint)
            }
        }
        if (selected) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Selected", tint = MintAccent)
        }
    }
}

@Composable
private fun LabeledSlider(label: String, value: Int, valueLabel: String, range: IntRange, onChange: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, color = MintAccent)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.size(12.dp))
        Switch(checked = checked, onCheckedChange = onCheck)
    }
}
