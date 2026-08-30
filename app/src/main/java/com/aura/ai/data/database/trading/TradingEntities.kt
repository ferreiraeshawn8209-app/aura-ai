package com.aura.ai.data.database.trading

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aura.ai.domain.trading.model.*
import java.time.LocalDateTime

@Entity(tableName = "market_assets")
data class MarketAssetEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val market: String,
    val assetClass: String,
    val currency: String
) {
    fun toDomain() = MarketAsset(symbol, name, market, assetClass, currency)
}

@Entity(tableName = "price_snapshots")
data class PriceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val price: Double,
    val timestamp: Long, // milliseconds
    val status: String, // LIVE, DELAYED, OFFLINE, MOCK
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double?,
    val volume: Long?
) {
    fun toDomain() = PriceSnapshot(
        symbol = symbol,
        price = price,
        timestamp = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        ),
        status = PriceStatus.valueOf(status),
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume
    )
}

@Entity(tableName = "technical_indicators")
data class TechnicalIndicatorsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val sma20: Double?,
    val sma50: Double?,
    val sma200: Double?,
    val ema12: Double?,
    val ema26: Double?,
    val rsi14: Double?,
    val macdLine: Double?,
    val macdSignal: Double?,
    val macdHistogram: Double?,
    val vwap: Double?,
    val bollingerBandUpper: Double?,
    val bollingerBandMiddle: Double?,
    val bollingerBandLower: Double?,
    val atr14: Double?,
    val adx: Double?,
    val timestamp: Long
) {
    fun toDomain() = TechnicalIndicators(
        symbol = symbol,
        sma20 = sma20,
        sma50 = sma50,
        sma200 = sma200,
        ema12 = ema12,
        ema26 = ema26,
        rsi14 = rsi14,
        macdLine = macdLine,
        macdSignal = macdSignal,
        macdHistogram = macdHistogram,
        vwap = vwap,
        bollingerBandUpper = bollingerBandUpper,
        bollingerBandMiddle = bollingerBandMiddle,
        bollingerBandLower = bollingerBandLower,
        atr14 = atr14,
        adx = adx,
        timestamp = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        )
    )
}

@Entity(tableName = "trade_signals")
data class TradeSignalEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val market: String,
    val direction: String, // BUY, SELL
    val auraScore: Int,
    val confidence: Double,
    val reason: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskRewardRatio: Double,
    val timestamp: Long,
    val dataStatus: String
) {
    fun toDomain() = TradeSignal(
        id = id,
        symbol = symbol,
        market = market,
        direction = TradeDirection.valueOf(direction),
        auraScore = auraScore,
        confidence = confidence,
        reason = reason,
        entryPrice = entryPrice,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        riskRewardRatio = riskRewardRatio,
        timestamp = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        ),
        dataStatus = PriceStatus.valueOf(dataStatus)
    )
}

@Entity(tableName = "trade_plans")
data class TradePlanEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val market: String,
    val direction: String,
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val positionSize: Int,
    val riskAmount: Double,
    val potentialProfit: Double,
    val riskRewardRatio: Double,
    val confidence: Double,
    val signalReason: String,
    val timestamp: Long,
    val dataStatus: String
) {
    fun toDomain() = TradePlan(
        id = id,
        symbol = symbol,
        market = market,
        direction = TradeDirection.valueOf(direction),
        entry = entry,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        positionSize = positionSize,
        riskAmount = riskAmount,
        potentialProfit = potentialProfit,
        riskRewardRatio = riskRewardRatio,
        confidence = confidence,
        signalReason = signalReason,
        timestamp = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestamp),
            java.time.ZoneId.systemDefault()
        ),
        dataStatus = PriceStatus.valueOf(dataStatus)
    )
}

@Entity(tableName = "paper_orders")
data class PaperOrderEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val direction: String,
    val quantity: Int,
    val entryPrice: Double,
    val status: String,
    val createdAt: Long,
    val executedAt: Long?,
    val executionPrice: Double?
) {
    fun toDomain() = PaperOrder(
        id = id,
        symbol = symbol,
        direction = TradeDirection.valueOf(direction),
        quantity = quantity,
        entryPrice = entryPrice,
        status = OrderStatus.valueOf(status),
        createdAt = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(createdAt),
            java.time.ZoneId.systemDefault()
        ),
        executedAt = executedAt?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                java.time.ZoneId.systemDefault()
            )
        },
        executionPrice = executionPrice
    )
}

@Entity(tableName = "positions")
data class PositionEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val openedAt: Long,
    val unrealisedPnL: Double,
    val unrealisedPnLPercent: Double
) {
    fun toDomain() = Position(
        id = id,
        symbol = symbol,
        quantity = quantity,
        entryPrice = entryPrice,
        currentPrice = currentPrice,
        stopLoss = stopLoss,
        takeProfit = takeProfit,
        openedAt = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(openedAt),
            java.time.ZoneId.systemDefault()
        ),
        unrealisedPnL = unrealisedPnL,
        unrealisedPnLPercent = unrealisedPnLPercent
    )
}

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey val id: String,
    val startingBalance: Double,
    val currentBalance: Double,
    val availableCash: Double,
    val totalUnrealisedPnL: Double,
    val totalRealisedPnL: Double,
    val dailyPnL: Double,
    val dailyPnLPercent: Double,
    val totalReturn: Double,
    val totalReturnPercent: Double,
    val winRate: Double,
    val numberOfTrades: Int,
    val lastUpdated: Long
) {
    fun toDomain() = Portfolio(
        id = id,
        startingBalance = startingBalance,
        currentBalance = currentBalance,
        availableCash = availableCash,
        totalUnrealisedPnL = totalUnrealisedPnL,
        totalRealisedPnL = totalRealisedPnL,
        dailyPnL = dailyPnL,
        dailyPnLPercent = dailyPnLPercent,
        totalReturn = totalReturn,
        totalReturnPercent = totalReturnPercent,
        winRate = winRate,
        numberOfTrades = numberOfTrades,
        lastUpdated = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(lastUpdated),
            java.time.ZoneId.systemDefault()
        )
    )
}

@Entity(tableName = "trading_challenges")
data class TradingChallengeEntity(
    @PrimaryKey val id: String,
    val startingCapital: Double,
    val targetCapital: Double,
    val durationDays: Int,
    val startedAt: Long,
    val currentPortfolioValue: Double
) {
    fun toDomain() = TradingChallenge(
        id = id,
        startingCapital = startingCapital,
        targetCapital = targetCapital,
        durationDays = durationDays,
        startedAt = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startedAt),
            java.time.ZoneId.systemDefault()
        ),
        currentPortfolioValue = currentPortfolioValue
    )
}

@Entity(tableName = "trading_settings")
data class TradingSettingsEntity(
    @PrimaryKey val id: String,
    val marketDataProvider: String,
    val paperTradingEnabled: Boolean,
    val startingBalance: Double,
    val enabledMarkets: String, // JSON list
    val selectedTimeframe: String,
    val maxRiskPerTrade: Double,
    val maxDailyLoss: Double,
    val maxOpenPositions: Int,
    val maxPositionSize: Int,
    val maxTradesPerDay: Int,
    val minimumAuraScore: Int,
    val mandatoryStopLoss: Boolean,
    val maxPortfolioExposure: Double,
    val maxDrawdown: Double,
    val tradingHaltThreshold: Double
) {
    fun toDomain(): TradingSettings {
        val markets = try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(enabledMarkets)
        } catch (e: Exception) {
            emptyList()
        }
        return TradingSettings(
            id = id,
            marketDataProvider = marketDataProvider,
            paperTradingEnabled = paperTradingEnabled,
            startingBalance = startingBalance,
            enabledMarkets = markets,
            selectedTimeframe = Timeframe.valueOf(selectedTimeframe),
            riskGovernor = RiskGovernor(
                maxRiskPerTrade = maxRiskPerTrade,
                maxDailyLoss = maxDailyLoss,
                maxOpenPositions = maxOpenPositions,
                maxPositionSize = maxPositionSize,
                maxTradesPerDay = maxTradesPerDay,
                minimumAuraScore = minimumAuraScore,
                mandatoryStopLoss = mandatoryStopLoss,
                maxPortfolioExposure = maxPortfolioExposure,
                maxDrawdown = maxDrawdown,
                tradingHaltThreshold = tradingHaltThreshold
            )
        )
    }
}

@Entity(tableName = "trading_performance")
data class TradingPerformanceEntity(
    @PrimaryKey val id: String,
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double,
    val averageWin: Double,
    val averageLoss: Double,
    val largestWin: Double,
    val largestLoss: Double,
    val profitFactor: Double,
    val maxDrawdown: Double,
    val sharpeRatio: Double?,
    val sortino: Double?,
    val totalPnL: Double,
    val percentReturn: Double
) {
    fun toDomain() = TradingPerformance(
        id = id,
        totalTrades = totalTrades,
        winningTrades = winningTrades,
        losingTrades = losingTrades,
        winRate = winRate,
        averageWin = averageWin,
        averageLoss = averageLoss,
        largestWin = largestWin,
        largestLoss = largestLoss,
        profitFactor = profitFactor,
        maxDrawdown = maxDrawdown,
        sharpeRatio = sharpeRatio,
        sortino = sortino,
        totalPnL = totalPnL,
        percentReturn = percentReturn
    )
}
