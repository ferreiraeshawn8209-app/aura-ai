package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.database.dao.ConversationDao
import com.aura.ai.data.database.dao.MemoryDao
import com.aura.ai.data.database.entity.ConversationEntity
import com.aura.ai.data.database.entity.MemoryEntity

@Database(
    entities = [ConversationEntity::class, MemoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun memoryDao(): MemoryDao
}
