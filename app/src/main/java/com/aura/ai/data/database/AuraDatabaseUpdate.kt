package com.aura.ai.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aura.ai.data.database.trading.*

/**
 * Update to existing AuraDatabase to include trading entities.
 * Add this to your existing database class.
 */
@Database(
    entities = [
        // Existing entities
        // ChatMessageEntity::class, MemoryEntity::class, etc.

        // Trading entities
        MarketAssetEntity::class,
        PriceSnapshotEntity::class,
        TechnicalIndicatorsEntity::class,
        TradeSignalEntity::class,
        TradePlanEntity::class,
        PaperOrderEntity::class,
        PositionEntity::class,
        PortfolioEntity::class,
        TradingChallengeEntity::class,
        TradingSettingsEntity::class,
        TradingPerformanceEntity::class
    ],
    version = 2 // Increment version for migration
)
abstract class AuraDatabase : RoomDatabase() {

    // Existing DAOs
    // abstract fun chatMessageDao(): ChatMessageDao
    // abstract fun memoryDao(): MemoryDao

    // Trading DAOs
    abstract fun marketAssetDao(): MarketAssetDao
    abstract fun priceSnapshotDao(): PriceSnapshotDao
    abstract fun technicalIndicatorsDao(): TechnicalIndicatorsDao
    abstract fun tradeSignalDao(): TradeSignalDao
    abstract fun tradePlanDao(): TradePlanDao
    abstract fun paperOrderDao(): PaperOrderDao
    abstract fun positionDao(): PositionDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun tradingChallengeDao(): TradingChallengeDao
    abstract fun tradingSettingsDao(): TradingSettingsDao
    abstract fun tradingPerformanceDao(): TradingPerformanceDao

    companion object {
        const val DB_NAME = "aura_ai.db"
    }
}
