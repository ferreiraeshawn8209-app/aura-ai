package com.aura.ai.di

import android.content.Context
import androidx.room.Room
import com.aura.ai.data.database.AuraDatabase
import com.aura.ai.data.database.trading.*
import com.aura.ai.domain.action.trading.DefaultTradingActionHandler
import com.aura.ai.domain.action.trading.TradingActionHandler
import com.aura.ai.domain.trading.data.*
import com.aura.ai.domain.trading.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TradingModule {

    // ========== DATABASE ==========

    @Singleton
    @Provides
    fun provideMarketAssetDao(database: AuraDatabase): MarketAssetDao {
        return database.marketAssetDao()
    }

    @Singleton
    @Provides
    fun providePriceSnapshotDao(database: AuraDatabase): PriceSnapshotDao {
        return database.priceSnapshotDao()
    }

    @Singleton
    @Provides
    fun provideTechnicalIndicatorsDao(database: AuraDatabase): TechnicalIndicatorsDao {
        return database.technicalIndicatorsDao()
    }

    @Singleton
    @Provides
    fun provideTradeSignalDao(database: AuraDatabase): TradeSignalDao {
        return database.tradeSignalDao()
    }

    @Singleton
    @Provides
    fun provideTradePlanDao(database: AuraDatabase): TradePlanDao {
        return database.tradePlanDao()
    }

    @Singleton
    @Provides
    fun providePaperOrderDao(database: AuraDatabase): PaperOrderDao {
        return database.paperOrderDao()
    }

    @Singleton
    @Provides
    fun providePositionDao(database: AuraDatabase): PositionDao {
        return database.positionDao()
    }

    @Singleton
    @Provides
    fun providePortfolioDao(database: AuraDatabase): PortfolioDao {
        return database.portfolioDao()
    }

    @Singleton
    @Provides
    fun provideTradingChallengeDao(database: AuraDatabase): TradingChallengeDao {
        return database.tradingChallengeDao()
    }

    @Singleton
    @Provides
    fun provideTradingSettingsDao(database: AuraDatabase): TradingSettingsDao {
        return database.tradingSettingsDao()
    }

    @Singleton
    @Provides
    fun provideTradingPerformanceDao(database: AuraDatabase): TradingPerformanceDao {
        return database.tradingPerformanceDao()
    }

    // ========== MARKET DATA ==========

    @Singleton
    @Provides
    fun provideMarketDataProvider(): MarketDataProvider {
        // Default to mock provider for development
        // Can be switched to real provider when API credentials are configured
        return MockMarketDataProvider()
    }

    // ========== BROKER ==========

    @Singleton
    @Provides
    fun providePaperTradingBroker(
        marketDataProvider: MarketDataProvider
    ): BrokerGateway {
        return PaperTradingBroker(
            initialBalance = 100.0,
            marketDataProvider = marketDataProvider,
            slippagePercent = 0.1
        )
    }

    // ========== USE CASES ==========

    @Singleton
    @Provides
    fun provideScanMarketUseCase(
        marketDataProvider: MarketDataProvider
    ): ScanMarketUseCase {
        return ScanMarketUseCase(marketDataProvider)
    }

    @Singleton
    @Provides
    fun provideRiskGovernorUseCase(): RiskGovernorUseCase {
        return RiskGovernorUseCase()
    }

    @Singleton
    @Provides
    fun provideTradingChallengeUseCase(): TradingChallengeUseCase {
        return TradingChallengeUseCase()
    }

    @Singleton
    @Provides
    fun providePaperTradingUseCase(
        riskGovernor: RiskGovernorUseCase
    ): PaperTradingUseCase {
        return PaperTradingUseCase(riskGovernor)
    }

    // ========== ACTION HANDLER ==========

    @Singleton
    @Provides
    fun provideTradingActionHandler(
        marketDataProvider: MarketDataProvider,
        broker: BrokerGateway,
        scanMarket: ScanMarketUseCase,
        riskGovernor: RiskGovernorUseCase,
        paperTrading: PaperTradingUseCase
    ): TradingActionHandler {
        return DefaultTradingActionHandler(
            marketDataProvider,
            broker,
            scanMarket,
            riskGovernor,
            paperTrading
        )
    }
}
