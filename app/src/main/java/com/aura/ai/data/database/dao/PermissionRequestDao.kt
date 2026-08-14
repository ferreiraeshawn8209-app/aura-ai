package com.aura.ai.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.ai.data.database.entity.PermissionRequestEntity

@Dao
interface PermissionRequestDao {
    @Insert
    suspend fun insert(request: PermissionRequestEntity)

    @Query("SELECT * FROM permission_requests ORDER BY requestedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int = 50): List<PermissionRequestEntity>
}
