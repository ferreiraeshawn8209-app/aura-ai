package com.aura.ai.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aura.ai.data.local.T1000Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideT1000Database(
        @ApplicationContext context: Context
    ): T1000Database {
        return Room.databaseBuilder(
            context,
            T1000Database::class.java,
            "t1000_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Singleton
    @Provides
    fun provideMemoryDao(database: T1000Database) = database.memoryDao()

    @Singleton
    @Provides
    fun provideSkillDao(database: T1000Database) = database.skillDao()

    @Singleton
    @Provides
    fun provideAgentActionDao(database: T1000Database) = database.agentActionDao()

    @Singleton
    @Provides
    fun provideTaskHistoryDao(database: T1000Database) = database.taskHistoryDao()

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """CREATE TABLE IF NOT EXISTS `task_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `request` TEXT NOT NULL, `planJson` TEXT NOT NULL, `outcomeJson` TEXT NOT NULL, `outcome` TEXT NOT NULL, `skillId` INTEGER, `startedAt` INTEGER NOT NULL, `finishedAt` INTEGER NOT NULL)""",
            )
        }
    }
}
