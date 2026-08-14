package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.database.dao.*
import com.aura.ai.data.database.entity.*

@Database(
    entities = [
        ConversationEntity::class,
        MemoryEntity::class,
        SkillEntity::class,
        SkillVersionEntity::class,
        SkillStatEntity::class,
        LearningEventEntity::class,
        AuditLogEntity::class,
        ImprovementProposalEntity::class,
        SandboxTestResultEntity::class,
        PermissionRequestEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun conversationDao(): com.aura.ai.data.database.dao.ConversationDao
    abstract fun memoryDao(): com.aura.ai.data.database.dao.MemoryDao

    abstract fun skillDao(): SkillDao
    abstract fun skillVersionDao(): SkillVersionDao
    abstract fun skillStatDao(): SkillStatDao
    abstract fun learningEventDao(): LearningEventDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun improvementProposalDao(): ImprovementProposalDao
    abstract fun sandboxTestResultDao(): SandboxTestResultDao
    abstract fun permissionRequestDao(): PermissionRequestDao
}
