package com.aura.ai.data.local

import androidx.room.Database
import androidx.room.TypeConverters
import com.aura.ai.data.local.dao.TaskHistoryDao
import com.aura.ai.data.local.entity.TaskHistoryEntity
import androidx.room.RoomDatabase
import com.aura.ai.data.local.dao.AgentActionDao
import com.aura.ai.data.local.dao.MemoryDao
import com.aura.ai.data.local.dao.SkillDao
import com.aura.ai.data.local.entity.AgentActionEntity
import com.aura.ai.data.local.entity.MemoryEntity
import com.aura.ai.data.local.entity.SkillEntity

@Database(
    entities = [
        MemoryEntity::class,
        SkillEntity::class,
        AgentActionEntity::class,
        TaskHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class T1000Database : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun skillDao(): SkillDao
    abstract fun agentActionDao(): AgentActionDao
    abstract fun taskHistoryDao(): TaskHistoryDao
}
