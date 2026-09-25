package com.aura.ai.di

import com.aura.ai.coding.data.repository.MockCodingRepository
import com.aura.ai.coding.domain.repository.ICodingRepository
import com.aura.ai.finance.data.repository.MockFinanceRepository
import com.aura.ai.finance.domain.repository.IFinanceRepository
import com.aura.ai.trading.data.repository.MockTradingRepository
import com.aura.ai.trading.domain.repository.ITradingRepository
import com.aura.ai.webagent.data.repository.MockBrowserRepository
import com.aura.ai.webagent.domain.repository.IBrowserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExternalIntegrationModule {
    @Binds
    @Singleton
    abstract fun bindFinanceRepository(impl: MockFinanceRepository): IFinanceRepository

    @Binds
    @Singleton
    abstract fun bindTradingRepository(impl: MockTradingRepository): ITradingRepository

    @Binds
    @Singleton
    abstract fun bindBrowserRepository(impl: MockBrowserRepository): IBrowserRepository

    @Binds
    @Singleton
    abstract fun bindCodingRepository(impl: MockCodingRepository): ICodingRepository
}
