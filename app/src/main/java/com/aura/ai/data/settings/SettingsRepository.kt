package com.aura.ai.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.ZoneOffset

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "t1000_settings")

/**
 * User-configurable runtime settings. The Ollama endpoint and model can be
 * changed here without rebuilding the app, satisfying the "configurable without
 * rebuilding" requirement.
 */
data class T1000Settings(
    val ollamaBaseUrl: String = DEFAULT_OLLAMA_URL,
    val selectedModel: String? = DEFAULT_MODEL,
    val requestTimeoutSeconds: Int = 120,
    val temperature: Int = 70, // stored as 0..100, divided by 100 at call time
    val autonomyEnabled: Boolean = true,
    val wakeWordEnabled: Boolean = false,
    val voiceRepliesEnabled: Boolean = true,
    val cloudEnabled: Boolean = false,
    val cloudBaseUrl: String = "https://openrouter.ai/api/v1",
    val cloudModel: String = "",
    val cloudRequestsPerDayLimit: Int = 60,
    val sharedFilesTreeUri: String? = null,
) {
    companion object {
        const val DEFAULT_OLLAMA_URL = "http://192.168.8.18:11434"
        const val DEFAULT_MODEL = "qwen3:1.7b"
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val BASE_URL = stringPreferencesKey("ollama_base_url")
        val MODEL = stringPreferencesKey("selected_model")
        val TIMEOUT = intPreferencesKey("request_timeout")
        val TEMPERATURE = intPreferencesKey("temperature")
        val AUTONOMY = booleanPreferencesKey("autonomy_enabled")
        val WAKE = booleanPreferencesKey("wake_word_enabled")
        val VOICE = booleanPreferencesKey("voice_replies_enabled")
        val CLOUD_ENABLED = booleanPreferencesKey("cloud_enabled")
        val CLOUD_BASE_URL = stringPreferencesKey("cloud_base_url")
        val CLOUD_MODEL = stringPreferencesKey("cloud_model")
        val CLOUD_DAILY_LIMIT = intPreferencesKey("cloud_daily_request_limit")
        val CLOUD_USAGE_DAY = stringPreferencesKey("cloud_usage_day_utc")
        val CLOUD_USAGE_COUNT = intPreferencesKey("cloud_usage_count")
        val SHARED_FILES_TREE_URI = stringPreferencesKey("shared_files_tree_uri")
    }

    val settings: Flow<T1000Settings> = context.dataStore.data.map { p ->
        // These loopback/emulator-only values cannot reach the PC Ollama server
        // from a physical phone. Keep any non-loopback, user-entered endpoint.
        val normalizedUrl = OllamaEndpointPolicy.normalize(
            storedUrl = p[Keys.BASE_URL],
            defaultUrl = T1000Settings.DEFAULT_OLLAMA_URL,
        )
        val storedModel = p[Keys.MODEL]
        val normalizedModel = if (storedModel == "gemma3:1b-cloud") {
            T1000Settings.DEFAULT_MODEL
        } else {
            storedModel ?: T1000Settings.DEFAULT_MODEL
        }
        T1000Settings(
            ollamaBaseUrl = normalizedUrl,
            selectedModel = normalizedModel,
            requestTimeoutSeconds = p[Keys.TIMEOUT] ?: 120,
            temperature = p[Keys.TEMPERATURE] ?: 70,
            autonomyEnabled = p[Keys.AUTONOMY] ?: true,
            wakeWordEnabled = p[Keys.WAKE] ?: false,
            voiceRepliesEnabled = p[Keys.VOICE] ?: true,
            cloudEnabled = p[Keys.CLOUD_ENABLED] ?: false,
            cloudBaseUrl = p[Keys.CLOUD_BASE_URL] ?: "https://openrouter.ai/api/v1",
            cloudModel = p[Keys.CLOUD_MODEL] ?: "",
            cloudRequestsPerDayLimit = p[Keys.CLOUD_DAILY_LIMIT] ?: 60,
            sharedFilesTreeUri = p[Keys.SHARED_FILES_TREE_URI],
        )
    }

    suspend fun setBaseUrl(url: String) = context.dataStore.edit { it[Keys.BASE_URL] = url.trim().trimEnd('/') }
    suspend fun setModel(model: String) = context.dataStore.edit { it[Keys.MODEL] = model }
    suspend fun setTimeout(seconds: Int) = context.dataStore.edit { it[Keys.TIMEOUT] = seconds.coerceIn(10, 600) }
    suspend fun setTemperature(value: Int) = context.dataStore.edit { it[Keys.TEMPERATURE] = value.coerceIn(0, 100) }
    suspend fun setAutonomy(enabled: Boolean) = context.dataStore.edit { it[Keys.AUTONOMY] = enabled }
    suspend fun setWakeWord(enabled: Boolean) = context.dataStore.edit { it[Keys.WAKE] = enabled }
    suspend fun setVoiceReplies(enabled: Boolean) = context.dataStore.edit { it[Keys.VOICE] = enabled }
    suspend fun setCloudSettings(enabled: Boolean, baseUrl: String, model: String) = context.dataStore.edit {
        it[Keys.CLOUD_ENABLED] = enabled
        it[Keys.CLOUD_BASE_URL] = baseUrl.trim().trimEnd('/')
        it[Keys.CLOUD_MODEL] = model.trim()
    }

    suspend fun setCloudDailyRequestLimit(limit: Int) = context.dataStore.edit {
        it[Keys.CLOUD_DAILY_LIMIT] = limit.coerceIn(1, 1000)
    }

    suspend fun setSharedFilesTreeUri(uri: String?) = context.dataStore.edit {
        if (uri.isNullOrBlank()) it.remove(Keys.SHARED_FILES_TREE_URI)
        else it[Keys.SHARED_FILES_TREE_URI] = uri
    }

    suspend fun reserveCloudRequest(limit: Int): Boolean {
        val today = LocalDate.now(ZoneOffset.UTC).toString()
        var accepted = false
        context.dataStore.edit { preferences ->
            val previousDay = preferences[Keys.CLOUD_USAGE_DAY]
            val previousCount = if (previousDay == today) preferences[Keys.CLOUD_USAGE_COUNT] ?: 0 else 0
            val next = CloudDailyQuota.nextCount(today, previousDay, previousCount, limit)
            preferences[Keys.CLOUD_USAGE_DAY] = today
            preferences[Keys.CLOUD_USAGE_COUNT] = next ?: previousCount.coerceAtLeast(0)
            if (next != null) accepted = true
        }
        return accepted
    }

    internal object CloudDailyQuota {
        fun nextCount(todayUtc: String, storedDayUtc: String?, storedCount: Int, limit: Int): Int? {
            val count = if (todayUtc == storedDayUtc) storedCount.coerceAtLeast(0) else 0
            val safeLimit = limit.coerceIn(1, 1000)
            return if (count >= safeLimit) null else count + 1
        }
    }
}

internal object OllamaEndpointPolicy {
    fun normalize(storedUrl: String?, defaultUrl: String): String = when (
        storedUrl?.trim()?.trimEnd('/')?.lowercase()
    ) {
        "http://10.0.2.2:11434",
        "http://0.0.0.0:11434",
        "http://localhost:11434",
        "http://127.0.0.1:11434" -> defaultUrl
        else -> storedUrl ?: defaultUrl
    }
}