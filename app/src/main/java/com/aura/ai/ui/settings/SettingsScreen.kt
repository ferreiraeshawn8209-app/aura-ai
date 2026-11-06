package com.aura.ai.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.data.remote.ollama.OllamaModel
import com.aura.ai.ui.theme.MintAccent
import com.aura.ai.ui.theme.RoseError
import com.aura.ai.ui.theme.TextFaint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var fileAccessMessage by remember { mutableStateOf<String?>(null) }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.saveSharedFilesTreeUri(uri.toString())
                fileAccessMessage = "Folder access saved. Aura can search filenames in this folder."
            }.onFailure {
                fileAccessMessage = "Android did not grant persistent folder access. Choose the folder again and allow access."
            }
        }
    }
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val conn by viewModel.conn.collectAsStateWithLifecycle()
    val voiceSelection by viewModel.voiceSelection.collectAsStateWithLifecycle()
    val cloud by viewModel.cloud.collectAsStateWithLifecycle()

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
                            "Aura uses your Ollama server. This phone is configured to reach your PC at 192.168.8.18. " +
                            "If your PC's network address changes, update the Base URL here.",
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
                            "No models discovered yet. Connect above and refresh the list of models installed on your PC.",
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
                Section("Cloud provider & fallback") {
                    Text(
                        "When enabled, Aura tries this OpenAI-compatible cloud endpoint first. If it fails or times out, Aura uses the selected local Ollama model. The API key is encrypted in Android secure storage and never shown again after saving.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ToggleRow(
                        "Cloud-first reasoning",
                        "Try cloud first, then automatically fall back to local Ollama.",
                        cloud.enabled,
                        viewModel::onCloudEnabledChange,
                    )
                    OutlinedTextField(
                        value = cloud.baseUrl,
                        onValueChange = viewModel::onCloudBaseUrlChange,
                        label = { Text("Cloud API base URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = cloud.model,
                        onValueChange = viewModel::onCloudModelChange,
                        label = { Text("Cloud model name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LabeledSlider(
                        label = "Maximum cloud requests per day",
                        value = settings.cloudRequestsPerDayLimit,
                        valueLabel = settings.cloudRequestsPerDayLimit.toString() + " requests",
                        range = 1..1000,
                        onChange = viewModel::setCloudDailyRequestLimit,
                    )
                    OutlinedTextField(
                        value = cloud.apiKeyDraft,
                        onValueChange = viewModel::onCloudApiKeyChange,
                        label = { Text("Cloud API key") },
                        placeholder = { if (cloud.apiKeyConfigured) Text("Saved securely; leave empty to keep it") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = viewModel::saveCloudSettings) { Text("Save cloud settings") }
                    cloud.message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Section("File access") {
                    Text(
                        "Choose one folder Aura may search by filename. Aura will not read file contents, edit files, or delete them.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (settings.sharedFilesTreeUri == null) "No folder selected"
                        else "A folder has been selected for filename search",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { folderPicker.launch(null) }) { Text("Choose folder") }
                        if (settings.sharedFilesTreeUri != null) {
                            OutlinedButton(onClick = {
                                runCatching {
                                    context.contentResolver.releasePersistableUriPermission(
                                        android.net.Uri.parse(settings.sharedFilesTreeUri),
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                    )
                                }
                                viewModel.clearSharedFilesTreeUri()
                                fileAccessMessage = "Aura's access to the selected folder was removed."
                            }) { Text("Remove access") }
                        }
                    }
                    fileAccessMessage?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Section("Behavior") {
                    ToggleRow("Autonomous mode", "Let T1000 plan and act without step-by-step confirmation.", settings.autonomyEnabled, viewModel::setAutonomy)
                    ToggleRow("Spoken replies", "Read answers aloud with text-to-speech.", settings.voiceRepliesEnabled, viewModel::setVoiceReplies)
                    ToggleRow("\"Yo, yo, yo\" wake phrase", "Keep Aura listening quietly for its wake phrase.", settings.wakeWordEnabled, viewModel::setWakeWord)
                }
            }

            item {
                Section("Voice") {
                    Text(
                        "Choose an Android voice or load voices from ElevenLabs. Aura saves one exact voice ID and reports if it becomes unavailable instead of switching voices. A deep Austrian English accent depends on the voice you select; pitch alone cannot create an accent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "ElevenLabs key is stored encrypted on this device and is never included in the APK source. For a shared or public app, use a server-side proxy and a restricted key; do not distribute an APK containing your personal credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                    )
                    OutlinedTextField(
                        value = voiceSelection.elevenLabsApiKeyDraft,
                        onValueChange = viewModel::onElevenLabsApiKeyChange,
                        label = { Text("ElevenLabs API key") },
                        placeholder = {
                            if (voiceSelection.elevenLabsApiKeyConfigured) Text("Saved securely; blank keeps it")
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = viewModel::saveElevenLabsApiKey) { Text("Save key") }
                        if (voiceSelection.elevenLabsApiKeyConfigured) {
                            OutlinedButton(onClick = viewModel::removeElevenLabsApiKey) { Text("Remove key") }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::refreshVoices, enabled = !voiceSelection.loading) {
                            Text(if (voiceSelection.loading) "Loading…" else "Load voices")
                        }
                        OutlinedButton(onClick = viewModel::previewVoice) { Text("Preview") }
                    }
                    voiceSelection.message?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    voiceSelection.voices.forEach { voice ->
                        VoiceRow(
                            title = "${voice.metadata["gender"] ?: "Voice"} · ${voice.name}",
                            subtitle = listOfNotNull(
                                voice.languageTag,
                                voice.metadata["accent"],
                                if (voice.providerId == "elevenlabs") "ElevenLabs" else "Android",
                            ).joinToString(" · "),
                            selected = voice.id == voiceSelection.selectedVoiceId,
                            onClick = { viewModel.selectVoice(voice.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
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
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = TextFaint)
        }
        if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = "Selected voice", tint = MintAccent)
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
