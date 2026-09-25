package com.aura.ai.di

import com.aura.ai.agent.core.AgentPipeline
import com.aura.ai.agent.core.ToolRegistry
import com.aura.ai.data.repository.AgentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgentModule {
    @Singleton
    @Provides
    fun provideToolRegistry(): ToolRegistry {
        return ToolRegistry()
    }

    @Singleton
    @Provides
    fun provideAgentPipeline(
        toolRegistry: ToolRegistry,
        agentRepository: AgentRepository
    ): AgentPipeline {
        return AgentPipeline(toolRegistry, agentRepository)
    }
}
