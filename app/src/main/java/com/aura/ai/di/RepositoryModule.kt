package com.aura.ai.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // Repositories with injectable constructors are resolved directly by Hilt.
    // Re-providing the same type from itself creates a dependency cycle.
}
