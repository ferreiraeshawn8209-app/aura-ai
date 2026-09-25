package com.aura.ai.di

import android.content.Context
import com.aura.ai.core.logging.T1000Logger
import com.aura.ai.core.permissions.PermissionManager
import com.aura.ai.core.security.SecureStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Singleton
    @Provides
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }

    @Singleton
    @Provides
    fun provideSecureStorage(
        @ApplicationContext context: Context
    ): SecureStorage {
        return SecureStorage(context)
    }

    @Singleton
    @Provides
    fun provideLogger(): T1000Logger {
        return T1000Logger()
    }
}
