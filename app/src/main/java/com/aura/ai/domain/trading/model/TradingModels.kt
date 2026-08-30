package com.aura.ai.domain.trading.model

import java.time.LocalDateTime

// ============================================================================
// MARKET DATA MODELS
// ============================================================================

data class MarketAsset(
    val symbol: String,
    val name: String,
    val market: String, // "JSE", "NASDAQ", "NYSE", etc.
    val assetClass: String, // "EQUITY", "ETF", etc.
    val currency: String = "ZAR"
)

data class PriceSnapshot(
    val symbol: String,
    val price: Double,
    val timestamp: LocalDateTime,
    val status: PriceStatus = PriceStatus.LIVE,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val volume: Long? = null,
    val previousClose: Double? = null
)

enum class PriceStatus {
    LIVE, DELAYED, OFFLINE, MOCK
}

data class OHLC(
    val symbol: String,
    val timeframe: Timeframe,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val timestamp: LocalDateTime
)

enum class Timeframe {
    ONE_MINUTE, FIVE_MINUTES, FIFTEEN_MINUTES, ONE_HOUR, FOUR_HOURS, ONE_DAY, ONE_WEEK
}

// ============================================================================
// TECHNICAL INDICATORS
// ============================================================================

data class TechnicalIndicators(
    val symbol: String,
    val sma20: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null,
    val ema12: Double? = null,
    val ema26: Double? = null,
    val rsi14: Double? = null,
    val macdLine: Double? = null,
    val macdSignal: Double? = null,
    val macdHistogram: Double? = null,
    val vwap: Double? = null,
    val bollingerBandUpper: Double? = null,
    val bollingerBandMiddle: Double? = null,
    val bollingerBandLower: Double? = null,
    val atr14: Double? = null,
    val adx: Double? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

// ============================================================================
// TRADING OPPORTUNITY & SIGNALS
// ============================================================================

data class TradeSignal(
    val id: String = "",
    val symbol: String,
    val market: String,
    val direction: TradeDirection,
    val auraScore: Int, // 0-100
    val confidence: Double, // 0.0-1.0
    val reason: String,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskRewardRatio: Double,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val dataStatus: PriceStatus = PriceStatus.LIVE
)

enum class TradeDirection {
    BUY, SELL
}

// ============================================================================
// TRADE PLAN
// ============================================================================

data class TradePlan(
    val id: String = "",
    val symbol: String,
    val market: String,
    val direction: TradeDirection,
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val positionSize: Int = 1, // Number of shares/units
    val riskAmount: Double,
    val potentialProfit: Double,
    val riskRewardRatio: Double,
    val confidence: Double,
    val signalReason: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val dataStatus: PriceStatus = PriceStatus.LIVE
)

// ============================================================================
// PAPER TRADING EXECUTION
// ============================================================================

data class PaperOrder(
    val id: String = "",
    val symbol: String,
    val direction: TradeDirection,
    val quantity: Int,
    val entryPrice: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val executedAt: LocalDateTime? = null,
    val executionPrice: Double? = null
)

enum class OrderStatus {
    PENDING, EXECUTED, REJECTED, CANCELLED
}

data class Position(
    val id: String = "",
    val symbol: String,
    val quantity: Int,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val openedAt: LocalDateTime = LocalDateTime.now(),
    val unrealisedPnL: Double = 0.0,
    val unrealisedPnLPercent: Double = 0.0
) {
    fun calculateUnrealisedPnL(price: Double): Double {
        return (price - entryPrice) * quantity
    }

    fun calculateUnrealisedPnLPercent(price: Double): Double {
        return if (entryPrice != 0.0) {
            ((price - entryPrice) / entryPrice) * 100
        } else 0.0
    }
}

// ============================================================================
// PORTFOLIO
// ============================================================================

data class Portfolio(
    val id: String = "",
    val startingBalance: Double,
    val currentBalance: Double,
    val availableCash: Double,
    val positions: List<Position> = emptyList(),
    val totalUnrealisedPnL: Double = 0.0,
    val totalRealisedPnL: Double = 0.0,
    val dailyPnL: Double = 0.0,
    val dailyPnLPercent: Double = 0.0,
    val totalReturn: Double = 0.0,
    val totalReturnPercent: Double = 0.0,
    val winRate: Double = 0.0, // 0.0-1.0
    val numberOfTrades: Int = 0,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    fun calculateTotalPortfolioValue(): Double = currentBalance + totalUnrealisedPnL

    fun calculateCAGR(daysSinceStart: Int): Double {
        if (daysSinceStart == 0 || startingBalance == 0.0) return 0.0
        val yearsElapsed = daysSinceStart / 365.0
        val endValue = calculateTotalPortfolioValue()
        return (Math.pow(endValue / startingBalance, 1.0 / yearsElapsed) - 1.0) * 100
    }
}

// ============================================================================
// TRADING CHALLENGE
// ============================================================================

data class TradingChallenge(
    val id: String = "",
    val startingCapital: Double,
    val targetCapital: Double,
    val durationDays: Int = 42, // 6 weeks
    val startedAt: LocalDateTime = LocalDateTime.now(),
    val currentPortfolioValue: Double = startingCapital
) {
    fun getRequiredTotalReturn(): Double = targetCapital - startingCapital
    fun getRequiredTotalReturnPercent(): Double = (targetCapital / startingCapital - 1.0) * 100
    fun getRequiredCAGR(): Double {
        val yearsElapsed = durationDays / 365.0
        return if (yearsElapsed > 0 && startingCapital > 0) {
            (Math.pow(targetCapital / startingCapital, 1.0 / yearsElapsed) - 1.0) * 100
        } else 0.0
    }

    fun getDaysElapsed(): Long {
        val now = LocalDateTime.now()
        return java.time.temporal.ChronoUnit.DAYS.between(startedAt, now)
    }

    fun getDaysRemaining(): Long = (durationDays - getDaysElapsed()).coerceAtLeast(0)
    fun getProgressPercent(): Double = (currentPortfolioValue / targetCapital) * 100.0
    fun getRemainingAmount(): Double = (targetCapital - currentPortfolioValue).coerceAtLeast(0.0)
    fun isAheadOfSchedule(): Boolean {
        val daysElapsed = getDaysElapsed().toDouble()
        if (daysElapsed == 0.0) return false
        val requiredPercentDaily = (getRequiredTotalReturnPercent() / durationDays)
        val actualPercentDaily = ((currentPortfolioValue / startingCapital - 1.0) * 100) / daysElapsed
        return actualPercentDaily >= requiredPercentDaily
    }
}

// ============================================================================
// RISK MANAGEMENT
// ============================================================================

data class RiskGovernor(
    val maxRiskPerTrade: Double = 100.0, // ZAR
    val maxDailyLoss: Double = 500.0,
    val maxOpenPositions: Int = 5,
    val maxPositionSize: Int = 100,
    val maxTradesPerDay: Int = 10,
    val minimumAuraScore: Int = 50,
    val mandatoryStopLoss: Boolean = true,
    val maxPortfolioExposure: Double = 0.8, // 80%
    val maxDrawdown: Double = 0.2, // 20%
    val tradingHaltThreshold: Double = 0.15 // 15% daily loss
)

data class TradingStatus(
    val isTradingHalted: Boolean = false,
    val haltReason: String? = null,
    val dailyLossIncurred: Double = 0.0,
    val tradesExecutedToday: Int = 0
)

// ============================================================================
// SETTINGS & CONFIGURATION
// ============================================================================

data class TradingSettings(
    val id: String = "",
    val marketDataProvider: String = "MOCK", // "MOCK", "ALPHA_VANTAGE", "IB_API", etc.
    val paperTradingEnabled: Boolean = true,
    val startingBalance: Double = 100.0,
    val enabledMarkets: List<String> = listOf("JSE", "NASDAQ"),
    val enabledAssets: List<String> = emptyList(),
    val selectedTimeframe: Timeframe = Timeframe.ONE_DAY,
    val riskGovernor: RiskGovernor = RiskGovernor()
)

// ============================================================================
// PERFORMANCE STATISTICS
// ============================================================================

data class TradingPerformance(
    val id: String = "",
    val totalTrades: Int = 0,
    val winningTrades: Int = 0,
    val losingTrades: Int = 0,
    val winRate: Double = 0.0,
    val averageWin: Double = 0.0,
    val averageLoss: Double = 0.0,
    val largestWin: Double = 0.0,
    val largestLoss: Double = 0.0,
    val profitFactor: Double = 0.0,
    val maxDrawdown: Double = 0.0,
    val sharpeRatio: Double? = null,
    val sortino: Double? = null,
    val totalPnL: Double = 0.0,
    val percentReturn: Double = 0.0
)
