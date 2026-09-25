package com.aura.ai.di

import com.aura.ai.data.repository.MemoryRepository
import com.aura.ai.data.repository.SkillRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideMemoryRepository(
        memoryRepository: MemoryRepository
    ): MemoryRepository = memoryRepository

    @Singleton
    @Provides
    fun provideSkillRepository(
        skillRepository: SkillRepository
    ): SkillRepository = skillRepository
}
