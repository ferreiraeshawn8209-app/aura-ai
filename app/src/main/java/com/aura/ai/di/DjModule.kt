package com.aura.ai.di

import com.aura.ai.dj.core.DjAutoPilotEngine
import com.aura.ai.dj.logging.DjSessionLogger
import com.aura.ai.dj.playback.PlaybackStateProvider
import com.aura.ai.dj.queue.QueueManager
import com.aura.ai.dj.safety.DjSafetyController
import com.aura.ai.dj.selection.BpmMatcher
import com.aura.ai.dj.selection.KeyMatcher
import com.aura.ai.dj.selection.TrackSelector
import com.aura.ai.dj.transition.CrossfadeController
import com.aura.ai.dj.transition.PhraseTransitionPlanner
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DjModule {
    @Singleton
    @Provides
    fun provideDjAutoPilotEngine(): DjAutoPilotEngine = DjAutoPilotEngine()

    @Singleton
    @Provides
    fun provideTrackSelector(): TrackSelector = TrackSelector()

    @Singleton
    @Provides
    fun provideBpmMatcher(): BpmMatcher = BpmMatcher()

    @Singleton
    @Provides
    fun provideKeyMatcher(): KeyMatcher = KeyMatcher()

    @Singleton
    @Provides
    fun providePhraseTransitionPlanner(
        bpmMatcher: BpmMatcher,
        keyMatcher: KeyMatcher
    ): PhraseTransitionPlanner = PhraseTransitionPlanner(bpmMatcher, keyMatcher)

    @Singleton
    @Provides
    fun provideCrossfadeController(): CrossfadeController = CrossfadeController()

    @Singleton
    @Provides
    fun provideQueueManager(): QueueManager = QueueManager()

    @Singleton
    @Provides
    fun providePlaybackStateProvider(): PlaybackStateProvider = PlaybackStateProvider()

    @Singleton
    @Provides
    fun provideDjSafetyController(
        djEngine: DjAutoPilotEngine,
        playbackStateProvider: PlaybackStateProvider
    ): DjSafetyController = DjSafetyController(djEngine, playbackStateProvider)

    @Singleton
    @Provides
    fun provideDjSessionLogger(
        logger: com.aura.ai.core.logging.T1000Logger
    ): DjSessionLogger = DjSessionLogger(logger)
}
