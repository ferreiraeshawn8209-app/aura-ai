package com.aura.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.data.remote.ollama.OllamaModel
import com.aura.ai.data.remote.ollama.OllamaRepository
import com.aura.ai.data.remote.ollama.OllamaResult
import com.aura.ai.data.settings.SettingsRepository
import com.aura.ai.data.settings.T1000Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import com.aura.ai.network.LocalHttpPolicy
import kotlinx.coroutines.flow.first

data class ConnectionUiState(
    val testing: Boolean = false,
    val discovering: Boolean = false,
    val connected: Boolean? = null,
    val serverVersion: String? = null,
    val models: List<OllamaModel> = emptyList(),
    val message: String? = null,
    val urlDraft: String = T1000Settings.DEFAULT_OLLAMA_URL,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val ollama: OllamaRepository,
) : ViewModel() {

    val settingsState: StateFlow<T1000Settings> =
        settings.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), T1000Settings())

    private val _conn = MutableStateFlow(ConnectionUiState())
    val conn: StateFlow<ConnectionUiState> = _conn.asStateFlow()

    init {
        viewModelScope.launch {
            settings.settings.collect { s -> _conn.update { it.copy(urlDraft = s.ollamaBaseUrl) } }
        }
    }

    fun onUrlDraftChange(value: String) = _conn.update { it.copy(urlDraft = value) }

    fun saveUrlAndTest() {
        if (_conn.value.testing || _conn.value.discovering) return
        val url = _conn.value.urlDraft.trim().trimEnd('/')
        val parsed = url.toHttpUrlOrNull()
        if (parsed == null || parsed.encodedPath != "/" || parsed.query != null ||
            parsed.fragment != null || parsed.username.isNotEmpty() || parsed.password.isNotEmpty() ||
            (parsed.scheme == "http" && !LocalHttpPolicy.isLocalHost(parsed.host))) {
            _conn.update { it.copy(connected = false, message = "Enter http://YOUR-PC-IP:11434, http://127.0.0.1:11434 for USB, or an HTTPS server URL.") }
            return
        }
        _conn.update { it.copy(testing = true, message = null, connected = null, serverVersion = null, models = emptyList()) }
        viewModelScope.launch {
            when (val result = ollama.testConnection(url)) {
                is OllamaResult.Success -> {
                    settings.setBaseUrl(url)
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
                is OllamaResult.Success -> {
                    val selected = settings.settings.first().selectedModel
                    if (result.value.none { it.name == selected }) {
                        settings.clearModel()
                        if (result.value.size == 1) settings.setModel(result.value.single().name)
                    }
                    _conn.update {
                        it.copy(
                            discovering = false,
                            models = result.value,
                            message = if (result.value.isEmpty()) "No models installed. Pull a local model on your PC, then refresh." else null,
                        )
                    }
                }
                is OllamaResult.Failure -> _conn.update {
                    it.copy(discovering = false, connected = false, message = result.error.message)
                }
            }
        }
    }

    fun selectModel(name: String) = viewModelScope.launch { settings.setModel(name) }
    fun setTimeout(seconds: Int) = viewModelScope.launch { settings.setTimeout(seconds) }
    fun setTemperature(value: Int) = viewModelScope.launch { settings.setTemperature(value) }
    fun setAutonomy(enabled: Boolean) = viewModelScope.launch { settings.setAutonomy(enabled) }
    fun setWakeWord(enabled: Boolean) = viewModelScope.launch { settings.setWakeWord(enabled) }
    fun setVoiceReplies(enabled: Boolean) = viewModelScope.launch { settings.setVoiceReplies(enabled) }
}
