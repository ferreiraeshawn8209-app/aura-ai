package com.aura.ai.data.mission

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.ai.agent.mission.DailyMission
import com.aura.ai.agent.mission.MissionCore
import com.aura.ai.agent.mission.MissionObjective
import com.aura.ai.agent.mission.MissionTask
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.missionDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "aura_permanent_mission")

/**
 * Local durable mission state, deliberately stored separately from chat history
 * and model context. The immutable charter remains in MissionCore; this store
 * keeps daily progress and the last reviewed date across app/model restarts.
 */
@Singleton
class MissionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DATE = stringPreferencesKey("daily_mission_date")
        val TASKS = stringPreferencesKey("daily_mission_tasks")
        val REVIEWED_AT = longPreferencesKey("daily_mission_reviewed_at")
    }

    val dailyMission: Flow<DailyMission?> = context.missionDataStore.data.map { prefs ->
        val date = prefs[Keys.DATE] ?: return@map null
        val rawTasks = prefs[Keys.TASKS].orEmpty()
        val tasks = rawTasks.lineSequence().mapNotNull { line ->
            val parts = line.split('|', limit = 4)
            if (parts.size != 4) return@mapNotNull null
            val objective = runCatching { MissionObjective.valueOf(parts[1]) }.getOrNull()
                ?: return@mapNotNull null
            MissionTask(
                id = parts[0],
                objective = objective,
                description = parts[2].replace("\\n", "\n"),
                completed = parts[3] == "1"
            )
        }.toList()
        DailyMission(date, tasks, prefs[Keys.REVIEWED_AT] ?: 0L)
    }

    suspend fun saveDailyMission(mission: DailyMission): Result<Unit> {
        val validated = MissionCore.validateDailyMission(mission)
        if (validated.isFailure) return Result.failure(validated.exceptionOrNull()!!)
        val encoded = mission.tasks.joinToString("\n") { task ->
            listOf(
                task.id.replace("|", ""),
                task.objective.name,
                task.description.replace("|", "").replace("\n", "\\n"),
                if (task.completed) "1" else "0"
            ).joinToString("|")
        }
        return runCatching {
            context.missionDataStore.edit { prefs ->
                prefs[Keys.DATE] = mission.date
                prefs[Keys.TASKS] = encoded
                prefs[Keys.REVIEWED_AT] = mission.reviewedAtEpochMillis
            }
            Unit
        }
    }

    suspend fun markTaskCompleted(taskId: String, completed: Boolean): Result<Unit> = runCatching {
        context.missionDataStore.edit { prefs ->
            val current = prefs[Keys.TASKS].orEmpty()
            prefs[Keys.TASKS] = current.lineSequence().map { line ->
                val parts = line.split('|', limit = 4)
                if (parts.size == 4 && parts[0] == taskId) {
                    parts.take(3).plus(if (completed) "1" else "0").joinToString("|")
                } else line
            }.joinToString("\n")
        }
        Unit
    }
}
