package com.aura.ai.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.voice.WakeWordService
import dagger.hilt.android.qualifiers.ApplicationContext
import com.aura.ai.data.remote.ollama.OllamaModel
import com.aura.ai.data.remote.ollama.OllamaRepository
import com.aura.ai.core.security.SecureStorage
import com.aura.ai.data.remote.ollama.OllamaResult
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.data.settings.T1000Settings
import com.aura.voice.AuraVoiceSubsystem
import com.aura.voice.SpeechSynthesisListener
import com.aura.voice.VoiceDescriptor
import com.aura.voice.VoiceError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectionUiState(
    val testing: Boolean = false,
    val discovering: Boolean = false,
    val connected: Boolean? = null,
    val serverVersion: String? = null,
    val models: List<OllamaModel> = emptyList(),
    val message: String? = null,
    val urlDraft: String = T1000Settings.DEFAULT_OLLAMA_URL,
)

data class VoiceSelectionUiState(
    val voices: List<VoiceDescriptor> = emptyList(),
    val selectedVoiceId: String? = null,
    val loading: Boolean = false,
    val message: String? = null,
)

data class CloudSettingsUiState(
    val enabled: Boolean = false,
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val model: String = "",
    val apiKeyDraft: String = "",
    val apiKeyConfigured: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val ollama: OllamaRepository,
    private val voice: AuraVoiceSubsystem,
    private val secureStorage: SecureStorage,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val settingsState: StateFlow<T1000Settings> =
        settings.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), T1000Settings())

    private val _conn = MutableStateFlow(ConnectionUiState())
    val conn: StateFlow<ConnectionUiState> = _conn.asStateFlow()

    private val _voiceSelection = MutableStateFlow(
        VoiceSelectionUiState(selectedVoiceId = voice.voiceSettings().voiceId),
    )
    val voiceSelection: StateFlow<VoiceSelectionUiState> = _voiceSelection.asStateFlow()

    private val _cloud = MutableStateFlow(CloudSettingsUiState())
    val cloud: StateFlow<CloudSettingsUiState> = _cloud.asStateFlow()

    init {
        viewModelScope.launch {
            settings.settings.collect { s ->
                _conn.update { it.copy(urlDraft = s.ollamaBaseUrl) }
                _cloud.update {
                    it.copy(
                        enabled = s.cloudEnabled,
                        baseUrl = s.cloudBaseUrl,
                        model = s.cloudModel,
                        apiKeyConfigured = secureStorage.hasSecret(com.aura.ai.agent.AgentOrchestrator.CLOUD_API_KEY_SECRET),
                    )
                }
            }
        }
    }

    fun onUrlDraftChange(value: String) = _conn.update { it.copy(urlDraft = value) }

    fun refreshVoices() {
        _voiceSelection.update { it.copy(loading = true, message = null) }
        voice.getVoices { voices ->
            _voiceSelection.update {
                it.copy(
                    voices = voices.sortedWith(compareBy({ it.languageTag }, { it.name })),
                    selectedVoiceId = voice.voiceSettings().voiceId,
                    loading = false,
                    message = if (voices.isEmpty()) "No Android voices are ready. Check the phone's Text-to-speech settings, then refresh." else null,
                )
            }
        }
    }

    fun selectVoice(voiceId: String) {
        voice.selectVoice(voiceId) { result ->
            if (result.accepted) {
                _voiceSelection.update { it.copy(selectedVoiceId = voiceId, message = "Voice selection saved on this device.") }
            } else {
                _voiceSelection.update { it.copy(message = result.error?.message ?: "Could not save the selected voice.") }
            }
        }
    }

    fun previewVoice() {
        val result = voice.speak(
            "All systems operational. Ready for your command.",
            object : SpeechSynthesisListener {
                override fun onStarted() = Unit
                override fun onCompleted() = Unit
                override fun onStopped() = Unit
                override fun onError(error: VoiceError) {
                    _voiceSelection.update { it.copy(message = error.message) }
                }
            },
        )
        if (!result.accepted) {
            _voiceSelection.update { it.copy(message = result.error?.message ?: "Voice preview failed.") }
        }
    }

    fun saveUrlAndTest() {
        val url = _conn.value.urlDraft.trim().trimEnd('/')
        viewModelScope.launch {
            settings.setBaseUrl(url)
            _conn.update { it.copy(testing = true, message = null, connected = null) }
            when (val result = ollama.testConnection(url)) {
                is OllamaResult.Success -> {
                    _conn.update { it.copy(testing = false, connected = true, serverVersion = result.value, message = "Connected to Ollama ${result.value}") }
                    discoverModels()
                }
                is OllamaResult.Failure -> _conn.update {
                    it.copy(testing = false, connected = false, message = result.error.message)
                }
            }
        }
    }

    fun discoverModels() {
        viewModelScope.launch {
            _conn.update { it.copy(discovering = true, message = null) }
            when (val result = ollama.listModels()) {
                is OllamaResult.Success -> _conn.update {
                    it.copy(
                        discovering = false,
                        models = result.value,
                        message = if (result.value.isEmpty()) "No models installed. Run `ollama pull llama3.2` on the host." else null,
                    )
                }
                is OllamaResult.Failure -> _conn.update {
                    it.copy(discovering = false, connected = false, message = result.error.message)
                }
            }
        }
    }

    fun onCloudBaseUrlChange(value: String) = _cloud.update { it.copy(baseUrl = value, message = null) }
    fun onCloudModelChange(value: String) = _cloud.update { it.copy(model = value, message = null) }
    fun onCloudApiKeyChange(value: String) = _cloud.update { it.copy(apiKeyDraft = value, message = null) }
    fun onCloudEnabledChange(value: Boolean) = _cloud.update { it.copy(enabled = value, message = null) }

    fun saveCloudSettings() = viewModelScope.launch {
        val draft = _cloud.value
        val key = draft.apiKeyDraft.trim()
        if (key.isNotEmpty()) secureStorage.saveSecret(com.aura.ai.agent.AgentOrchestrator.CLOUD_API_KEY_SECRET, key)
        settings.setCloudSettings(draft.enabled, draft.baseUrl, draft.model)
        _cloud.update {
            it.copy(
                apiKeyDraft = "",
                apiKeyConfigured = it.apiKeyConfigured || key.isNotEmpty(),
                message = if (draft.enabled && (draft.model.isBlank() || !(it.apiKeyConfigured || key.isNotEmpty())))
                    "Add a model and API key before cloud-first routing can be used."
                else "Cloud settings saved. Cloud will be tried first; Ollama remains the automatic fallback.",
            )
        }
    }

    fun selectModel(name: String) = viewModelScope.launch { settings.setModel(name) }
    fun setTimeout(seconds: Int) = viewModelScope.launch { settings.setTimeout(seconds) }
    fun setTemperature(value: Int) = viewModelScope.launch { settings.setTemperature(value) }
    fun setAutonomy(enabled: Boolean) = viewModelScope.launch { settings.setAutonomy(enabled) }
    fun setWakeWord(enabled: Boolean) = viewModelScope.launch {
        settings.setWakeWord(enabled)
        if (enabled) WakeWordService.start(context) else WakeWordService.stop(context)
    }
    fun setVoiceReplies(enabled: Boolean) = viewModelScope.launch { settings.setVoiceReplies(enabled) }
}
