package com.aura.ai.data.database

import android.content.ContentValues
import android.database.Cursor
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AuraDatabase::class.java.canonicalName
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesConversationAndMemoryAndCreatesNewTables() {
        val dbName = "aura_database"

        // Create v1 database and insert rows
        var db = helper.createDatabase(dbName, 1).apply {
            // create conversations & memory tables as v1 schema expects
            execSQL("""
                CREATE TABLE IF NOT EXISTS `conversations` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `role` TEXT NOT NULL,
                  `content` TEXT NOT NULL,
                  `timestamp` INTEGER NOT NULL
                )
            """.trimIndent())
            execSQL("""
                CREATE TABLE IF NOT EXISTS `memory` (
                  `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  `key` TEXT NOT NULL,
                  `value` TEXT NOT NULL,
                  `timestamp` INTEGER NOT NULL
                )
            """.trimIndent())

            // insert sample data
            execSQL("INSERT INTO conversations (role, content, timestamp) VALUES ('user','hello', 1234567890)")
            execSQL("INSERT INTO memory (`key`, `value`, timestamp) VALUES ('pref','morning', 1234567890)")

            // close v1
            close()
        }

        // Re-open using Room and run migration
        db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        // Validate that old rows survived
        val cursor: Cursor = db.query("SELECT id, role, content, timestamp FROM conversations")
        assertNotNull(cursor)
        cursor.use {
            it.moveToFirst()
            assertEquals("user", it.getString(1))
            assertEquals("hello", it.getString(2))
        }

        val cursor2: Cursor = db.query("SELECT id, `key`, `value` FROM memory")
        cursor2.use {
            it.moveToFirst()
            assertEquals("pref", it.getString(1))
            assertEquals("morning", it.getString(2))
        }

        // Check that new tables exist by querying sqlite_master
        val newTables = listOf("skills", "skill_versions", "skill_stats", "learning_events", "improvement_proposals", "sandbox_test_results", "permission_requests", "audit_log")
        val cursorMaster = db.query("SELECT name FROM sqlite_master WHERE type='table'")
        val names = mutableSetOf<String>()
        cursorMaster.use { c ->
            while (c.moveToNext()) {
                names.add(c.getString(0))
            }
        }

        for (t in newTables) {
            if (!names.contains(t)) {
                throw AssertionError("Expected table $t to exist after migration")
            }
        }

        // close after validations
        db.close()
    }

    companion object {
        // Duplicate of the migration used in AppModule to ensure MigrationTestHelper can run it
        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skills` (
                      `name` TEXT NOT NULL,
                      `purpose` TEXT NOT NULL,
                      `currentVersion` TEXT NOT NULL,
                      `active` INTEGER NOT NULL,
                      `createdAt` INTEGER NOT NULL,
                      `updatedAt` INTEGER NOT NULL,
                      PRIMARY KEY(`name`)
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skill_versions` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `metadataJson` TEXT NOT NULL,
                      `codeRef` TEXT,
                      `createdAt` INTEGER NOT NULL,
                      `testPassed` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `skill_stats` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `successfulRuns` INTEGER NOT NULL,
                      `failedRuns` INTEGER NOT NULL,
                      `averageConfidence` REAL NOT NULL,
                      `lastObservedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_events` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `eventType` TEXT NOT NULL,
                      `sourceSkill` TEXT,
                      `detailsJson` TEXT NOT NULL,
                      `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `improvement_proposals` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `title` TEXT NOT NULL,
                      `skillName` TEXT,
                      `description` TEXT NOT NULL,
                      `proposalJson` TEXT NOT NULL,
                      `createdBy` TEXT NOT NULL,
                      `approved` INTEGER NOT NULL,
                      `approvedBy` TEXT,
                      `createdAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sandbox_test_results` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `skillName` TEXT NOT NULL,
                      `version` TEXT NOT NULL,
                      `passed` INTEGER NOT NULL,
                      `metricsJson` TEXT NOT NULL,
                      `runAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `permission_requests` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `permission` TEXT NOT NULL,
                      `reason` TEXT NOT NULL,
                      `requestedBy` TEXT NOT NULL,
                      `granted` INTEGER NOT NULL,
                      `handledAt` INTEGER,
                      `requestedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_log` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `eventType` TEXT NOT NULL,
                      `eventSource` TEXT NOT NULL,
                      `detailsJson` TEXT NOT NULL,
                      `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
