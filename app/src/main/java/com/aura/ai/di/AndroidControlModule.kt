package com.aura.ai.di

import android.content.Context
import com.aura.ai.androidcontrol.applications.ApplicationControl
import com.aura.ai.androidcontrol.device.DeviceControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidControlModule {
    @Singleton
    @Provides
    fun provideApplicationControl(
        @ApplicationContext context: Context
    ): ApplicationControl {
        return ApplicationControl(context)
    }

    @Singleton
    @Provides
    fun provideDeviceControl(
        @ApplicationContext context: Context
    ): DeviceControl {
        return DeviceControl(context)
    }
}
