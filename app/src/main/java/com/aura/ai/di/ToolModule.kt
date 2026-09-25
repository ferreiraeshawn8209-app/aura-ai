package com.aura.ai.di

import com.aura.ai.agent.tool.CalculatorTool
import com.aura.ai.agent.tool.DateTimeTool
import com.aura.ai.agent.tool.RecallMemoryTool
import com.aura.ai.agent.tool.RecallSkillTool
import com.aura.ai.agent.tool.RememberTool
import com.aura.ai.agent.tool.Tool
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolModule {
    @Binds @IntoSet abstract fun recallMemory(tool: RecallMemoryTool): Tool
    @Binds @IntoSet abstract fun remember(tool: RememberTool): Tool
    @Binds @IntoSet abstract fun recallSkill(tool: RecallSkillTool): Tool
    @Binds @IntoSet abstract fun dateTime(tool: DateTimeTool): Tool
    @Binds @IntoSet abstract fun calculator(tool: CalculatorTool): Tool
}
