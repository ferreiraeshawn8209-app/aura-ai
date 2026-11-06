package com.aura.ai.di

import com.aura.ai.agent.tool.CalculatorTool
import com.aura.ai.agent.tool.PrepareContactCallTool
import com.aura.ai.agent.tool.PrepareContactSmsTool
import com.aura.ai.agent.tool.DateTimeTool
import com.aura.ai.agent.tool.OpenAppTool
import com.aura.ai.agent.tool.OpenWebsiteTool
import com.aura.ai.agent.tool.PreparePhoneCallTool
import com.aura.ai.agent.tool.PrepareSmsTool
import com.aura.ai.agent.tool.RecallMemoryTool
import com.aura.ai.agent.tool.RecallSkillTool
import com.aura.ai.agent.tool.RememberTool
import com.aura.ai.agent.tool.SearchContactsTool
import com.aura.ai.agent.tool.Tool
import com.aura.ai.agent.tool.WhatsAppDraftTool
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
    @Binds @IntoSet abstract fun whatsappDraft(tool: WhatsAppDraftTool): Tool
    @Binds @IntoSet abstract fun openApp(tool: OpenAppTool): Tool
    @Binds @IntoSet abstract fun openWebsite(tool: OpenWebsiteTool): Tool
    @Binds @IntoSet abstract fun preparePhoneCall(tool: PreparePhoneCallTool): Tool
    @Binds @IntoSet abstract fun prepareSms(tool: PrepareSmsTool): Tool
    @Binds @IntoSet abstract fun searchContacts(tool: SearchContactsTool): Tool
    @Binds @IntoSet abstract fun prepareContactCall(tool: PrepareContactCallTool): Tool
    @Binds @IntoSet abstract fun prepareContactSms(tool: PrepareContactSmsTool): Tool
}
