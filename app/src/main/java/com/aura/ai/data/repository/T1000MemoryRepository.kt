package com.aura.ai.data.repository

import com.aura.ai.data.local.dao.MemoryDao
import com.aura.ai.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class T1000MemoryRepository @Inject constructor(
    private val memoryDao: MemoryDao,
) {
    suspend fun rememberFact(type: String, content: String, importance: Int = 5): Long =
        memoryDao.insertMemory(
            MemoryEntity(
                type = type,
                content = content,
                importance = importance.coerceIn(1, 10),
            ),
        )

    suspend fun remember(content: String, type: String, importance: Float, source: String): Long =
        rememberFact(type, content, (importance * 10f).toInt().coerceIn(1, 10))

    suspend fun recall(query: String): List<MemoryEntity> =
        if (query.isBlank()) memoryDao.getTopMemories(20).first()
        else memoryDao.searchMemories(query).first()

    fun observeAll(): Flow<List<MemoryEntity>> = memoryDao.getTopMemories(100)

    suspend fun forget(id: Long) {
        memoryDao.getMemoryById(id)?.let { memoryDao.deleteMemory(it) }
    }
}