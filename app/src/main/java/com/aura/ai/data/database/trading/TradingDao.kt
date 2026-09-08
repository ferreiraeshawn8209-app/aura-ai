package com.aura.ai.data.database.trading

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketAssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: MarketAssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assets: List<MarketAssetEntity>)

    @Query("SELECT * FROM market_assets WHERE symbol = :symbol")
    suspend fun getAsset(symbol: String): MarketAssetEntity?

    @Query("SELECT * FROM market_assets WHERE market = :market")
    fun getAssetsByMarket(market: String): Flow<List<MarketAssetEntity>>
}

@Dao
interface PriceSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: PriceSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<PriceSnapshotEntity>)

    @Query("SELECT * FROM price_snapshots WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPrice(symbol: String): PriceSnapshotEntity?

    @Query("SELECT * FROM price_snapshots WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getPriceHistory(symbol: String, limit: Int = 100): List<PriceSnapshotEntity>
}

@Dao
interface TechnicalIndicatorsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(indicators: TechnicalIndicatorsEntity)

    @Query("SELECT * FROM technical_indicators WHERE symbol = :symbol ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestIndicators(symbol: String): TechnicalIndicatorsEntity?
}

@Dao
interface TradeSignalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signal: TradeSignalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<TradeSignalEntity>)

    @Query("SELECT * FROM trade_signals WHERE id = :id")
    suspend fun getSignal(id: String): TradeSignalEntity?

    @Query("SELECT * FROM trade_signals ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSignals(limit: Int = 50): Flow<List<TradeSignalEntity>>
}

@Dao
interface TradePlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: TradePlanEntity)

    @Query("SELECT * FROM trade_plans WHERE id = :id")
    suspend fun getPlan(id: String): TradePlanEntity?

    @Query("SELECT * FROM trade_plans ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPlans(limit: Int = 50): Flow<List<TradePlanEntity>>
}

@Dao
interface PaperOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: PaperOrderEntity)

    @Update
    suspend fun update(order: PaperOrderEntity)

    @Query("SELECT * FROM paper_orders WHERE id = :id")
    suspend fun getOrder(id: String): PaperOrderEntity?

    @Query("SELECT * FROM paper_orders WHERE status = :status ORDER BY createdAt DESC LIMIT :limit")
    fun getOrdersByStatus(status: String, limit: Int = 100): Flow<List<PaperOrderEntity>>

    @Query("SELECT * FROM paper_orders ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getOrderHistory(limit: Int = 100): List<PaperOrderEntity>
}

@Dao
interface PositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(position: PositionEntity)

    @Update
    suspend fun update(position: PositionEntity)

    @Query("SELECT * FROM positions WHERE id = :id")
    suspend fun getPosition(id: String): PositionEntity?

    @Query("SELECT * FROM positions WHERE symbol = :symbol")
    suspend fun getPositionBySymbol(symbol: String): PositionEntity?

    @Query("SELECT * FROM positions")
    fun getAllPositions(): Flow<List<PositionEntity>>

    @Query("DELETE FROM positions WHERE id = :id")
    suspend fun deletePosition(id: String)
}

@Dao
interface PortfolioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portfolio: PortfolioEntity)

    @Update
    suspend fun update(portfolio: PortfolioEntity)

    @Query("SELECT * FROM portfolios WHERE id = :id")
    fun getPortfolio(id: String): Flow<PortfolioEntity?>

    @Query("SELECT * FROM portfolios LIMIT 1")
    fun getLatestPortfolio(): Flow<PortfolioEntity?>
}

@Dao
interface TradingChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: TradingChallengeEntity)

    @Update
    suspend fun update(challenge: TradingChallengeEntity)

    @Query("SELECT * FROM trading_challenges WHERE id = :id")
    fun getChallenge(id: String): Flow<TradingChallengeEntity?>

    @Query("SELECT * FROM trading_challenges LIMIT 1")
    fun getLatestChallenge(): Flow<TradingChallengeEntity?>
}

@Dao
interface TradingSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: TradingSettingsEntity)

    @Update
    suspend fun update(settings: TradingSettingsEntity)

    @Query("SELECT * FROM trading_settings LIMIT 1")
    fun getSettings(): Flow<TradingSettingsEntity?>
}

@Dao
interface TradingPerformanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(performance: TradingPerformanceEntity)

    @Update
    suspend fun update(performance: TradingPerformanceEntity)

    @Query("SELECT * FROM trading_performance LIMIT 1")
    fun getPerformance(): Flow<TradingPerformanceEntity?>
}
