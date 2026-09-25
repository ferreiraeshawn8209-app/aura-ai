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

    fun selectModel(name: String) = viewModelScope.launch { settings.setModel(name) }
    fun setTimeout(seconds: Int) = viewModelScope.launch { settings.setTimeout(seconds) }
    fun setTemperature(value: Int) = viewModelScope.launch { settings.setTemperature(value) }
    fun setAutonomy(enabled: Boolean) = viewModelScope.launch { settings.setAutonomy(enabled) }
    fun setWakeWord(enabled: Boolean) = viewModelScope.launch { settings.setWakeWord(enabled) }
    fun setVoiceReplies(enabled: Boolean) = viewModelScope.launch { settings.setVoiceReplies(enabled) }
}
