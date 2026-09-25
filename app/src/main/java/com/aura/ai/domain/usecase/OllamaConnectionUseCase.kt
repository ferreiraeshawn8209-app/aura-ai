package com.aura.ai.domain.usecase

import com.aura.ai.data.remote.OllamaApi
import com.aura.ai.domain.model.OllamaConnectionState
import com.aura.ai.domain.model.OllamaModelsResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OllamaConnectionUseCase @Inject constructor(
    private val ollamaApi: OllamaApi
) {
    private val _connectionState = MutableStateFlow(OllamaConnectionState.DISCONNECTED)
    val connectionState: StateFlow<OllamaConnectionState> = _connectionState.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    suspend fun testConnection(): Boolean {
        return try {
            _connectionState.emit(OllamaConnectionState.CONNECTING)
            val version = ollamaApi.getVersion()
            _connectionState.emit(OllamaConnectionState.CONNECTED)
            true
        } catch (e: Exception) {
            _connectionState.emit(OllamaConnectionState.ERROR)
            false
        }
    }

    suspend fun discoverModels(): List<String> {
        return try {
            _connectionState.emit(OllamaConnectionState.CONNECTING)
            val response: OllamaModelsResponse = ollamaApi.listModels()
            val modelNames = response.models.map { it.name }
            _availableModels.emit(modelNames)
            if (modelNames.isNotEmpty()) {
                _selectedModel.emit(modelNames.first())
                _connectionState.emit(OllamaConnectionState.CONNECTED)
            } else {
                _connectionState.emit(OllamaConnectionState.MODEL_UNAVAILABLE)
            }
            modelNames
        } catch (e: Exception) {
            _connectionState.emit(OllamaConnectionState.ERROR)
            emptyList()
        }
    }

    suspend fun selectModel(modelName: String) {
        _selectedModel.emit(modelName)
    }

    fun getConnectionState(): OllamaConnectionState = _connectionState.value
    fun getSelectedModel(): String? = _selectedModel.value
    fun getAvailableModels(): List<String> = _availableModels.value
}
