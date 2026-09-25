package com.aura.ai.di

import android.content.Context
import com.aura.ai.voice.recognition.SpeechRecognitionManager
import com.aura.ai.voice.tts.TextToSpeechManager
import com.aura.ai.voice.wake.WakeWordDetector
import com.aura.ai.voice.state.VoiceStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoiceModule {
    @Singleton
    @Provides
    fun provideSpeechRecognitionManager(
        @ApplicationContext context: Context
    ): SpeechRecognitionManager {
        return SpeechRecognitionManager(context)
    }

    @Singleton
    @Provides
    fun provideTextToSpeechManager(
        @ApplicationContext context: Context
    ): TextToSpeechManager {
        return TextToSpeechManager(context)
    }

    @Singleton
    @Provides
    fun provideWakeWordDetector(
        @ApplicationContext context: Context
    ): WakeWordDetector {
        return WakeWordDetector(context)
    }

    @Singleton
    @Provides
    fun provideVoiceStateManager(): VoiceStateManager {
        return VoiceStateManager()
    }
}
