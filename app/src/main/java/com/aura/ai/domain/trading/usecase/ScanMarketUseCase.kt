package com.aura.ai.domain.trading.usecase

import com.aura.ai.domain.trading.data.MarketDataProvider
import com.aura.ai.domain.trading.model.*
import kotlin.math.sqrt

/**
 * Scans market for trading opportunities using technical analysis.
 */
class ScanMarketUseCase(
    private val marketDataProvider: MarketDataProvider
) {
    suspend fun scanAssets(symbols: List<String>, timeframe: Timeframe = Timeframe.ONE_DAY): Result<List<TradeSignal>> {
        return try {
            val signals = symbols.mapNotNull { symbol ->
                analyzeSymbol(symbol, timeframe).getOrNull()
            }
            Result.success(signals.sortedByDescending { it.auraScore })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun analyzeSymbol(symbol: String, timeframe: Timeframe): Result<TradeSignal?> {
        return try {
            // Get OHLC data
            val ohlcResult = marketDataProvider.getOHLC(symbol, timeframe, 100)
            if (ohlcResult.isFailure) return Result.success(null)

            val ohlcData = ohlcResult.getOrNull() ?: return Result.success(null)
            if (ohlcData.isEmpty()) return Result.success(null)

            val currentCandle = ohlcData.last()
            val price = currentCandle.close

            // Calculate indicators
            val indicators = calculateTechnicalIndicators(ohlcData)
            val score = calculateAuraScore(indicators, ohlcData)

            // Determine signal if score is above threshold
            if (score < 50) return Result.success(null)

            val direction = determineDirection(indicators, ohlcData)
            val (entry, stop, target) = calculateLevels(price, direction, indicators, ohlcData)
            val riskReward = if (direction == TradeDirection.BUY) {
                (target - entry) / (entry - stop)
            } else {
                (entry - target) / (stop - entry)
            }

            Result.success(
                TradeSignal(
                    symbol = symbol,
                    market = "MOCK",
                    direction = direction,
                    auraScore = score,
                    confidence = score / 100.0,
                    reason = generateReason(indicators, direction),
                    entryPrice = entry,
                    stopLoss = stop,
                    takeProfit = target,
                    riskRewardRatio = riskReward,
                    timestamp = java.time.LocalDateTime.now(),
                    dataStatus = PriceStatus.MOCK
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateTechnicalIndicators(ohlcData: List<OHLC>): TechnicalIndicators {
        val closes = ohlcData.map { it.close }
        val symbol = ohlcData.first().symbol

        return TechnicalIndicators(
            symbol = symbol,
            sma20 = calculateSMA(closes, 20),
            sma50 = calculateSMA(closes, 50),
            sma200 = calculateSMA(closes, 200),
            rsi14 = calculateRSI(closes, 14),
            macdLine = calculateEMA(closes, 12),
            macdSignal = calculateEMA(closes, 26),
            bollingerBandUpper = calculateBollingerUpper(closes, 20, 2.0),
            bollingerBandMiddle = calculateSMA(closes, 20),
            bollingerBandLower = calculateBollingerLower(closes, 20, 2.0)
        )
    }

    private fun calculateAuraScore(indicators: TechnicalIndicators, ohlcData: List<OHLC>): Int {
        var score = 50 // Base score

        // RSI analysis (0-30 oversold = bullish, 70-100 overbought = bearish)
        indicators.rsi14?.let { rsi ->
            when {
                rsi < 30 -> score += 15
                rsi > 70 -> score -= 15
                rsi in 40.0..60.0 -> score += 5
            }
        }

        // Price vs Moving averages
        val lastClose = ohlcData.last().close
        indicators.sma20?.let { sma20 ->
            if (lastClose > sma20) score += 10 else score -= 10
        }
        indicators.sma50?.let { sma50 ->
            if (lastClose > sma50) score += 8 else score -= 8
        }

        // Trend strength from MACD
        val macdDiff = (indicators.macdLine ?: 0.0) - (indicators.macdSignal ?: 0.0)
        if (macdDiff > 0) score += 12 else score -= 12

        // Volume trend
        if (ohlcData.size >= 2) {
            val recentVolume = ohlcData.takeLast(5).sumOf { it.volume }.toDouble() / 5
            val priorVolume = ohlcData.dropLast(5).takeLast(5).sumOf { it.volume }.toDouble() / 5
            if (recentVolume > priorVolume) score += 8
        }

        return score.coerceIn(0, 100)
    }

    private fun determineDirection(indicators: TechnicalIndicators, ohlcData: List<OHLC>): TradeDirection {
        val lastClose = ohlcData.last().close
        val rsi = indicators.rsi14 ?: 50.0
        val priceAboveSMA = lastClose > (indicators.sma50 ?: lastClose)
        val macdPositive = (indicators.macdLine ?: 0.0) > (indicators.macdSignal ?: 0.0)

        return if (rsi < 50 && priceAboveSMA && macdPositive) {
            TradeDirection.BUY
        } else {
            TradeDirection.SELL
        }
    }

    private fun calculateLevels(
        price: Double,
        direction: TradeDirection,
        indicators: TechnicalIndicators,
        ohlcData: List<OHLC>
    ): Triple<Double, Double, Double> {
        val atr = calculateATR(ohlcData, 14)
        return when (direction) {
            TradeDirection.BUY -> {
                val entry = price
                val stop = (price - atr * 2).coerceAtLeast(indicators.bollingerBandLower ?: price * 0.95)
                val target = price + atr * 3
                Triple(entry, stop, target)
            }
            TradeDirection.SELL -> {
                val entry = price
                val stop = (price + atr * 2).coerceAtMost(indicators.bollingerBandUpper ?: price * 1.05)
                val target = price - atr * 3
                Triple(entry, stop, target)
            }
        }
    }

    private fun generateReason(indicators: TechnicalIndicators, direction: TradeDirection): String {
        val rsi = indicators.rsi14?.toInt() ?: 50
        val trend = if (direction == TradeDirection.BUY) "Bullish" else "Bearish"
        return "$trend setup. RSI: $rsi, Price above key moving averages, Strong volume."
    }

    // ========== TECHNICAL INDICATOR CALCULATIONS ==========

    private fun calculateSMA(prices: List<Double>, period: Int): Double? {
        return if (prices.size >= period) {
            prices.takeLast(period).average()
        } else null
    }

    private fun calculateEMA(prices: List<Double>, period: Int): Double? {
        if (prices.size < period) return null
        var ema = prices.take(period).average()
        val multiplier = 2.0 / (period + 1)
        for (i in period until prices.size) {
            ema = prices[i] * multiplier + ema * (1 - multiplier)
        }
        return ema
    }

    private fun calculateRSI(prices: List<Double>, period: Int): Double? {
        if (prices.size < period + 1) return null
        var gains = 0.0
        var losses = 0.0
        for (i in 1..period) {
            val diff = prices[prices.size - period + i] - prices[prices.size - period + i - 1]
            if (diff > 0) gains += diff else losses += -diff
        }
        val avgGain = gains / period
        val avgLoss = losses / period
        return if (avgLoss == 0.0) 100.0 else 100 - (100 / (1 + avgGain / avgLoss))
    }

    private fun calculateBollingerUpper(prices: List<Double>, period: Int, stdDev: Double): Double? {
        return calculateSMA(prices, period)?.let { sma ->
            val variance = prices.takeLast(period).map { (it - sma) * (it - sma) }.average()
            sma + sqrt(variance) * stdDev
        }
    }

    private fun calculateBollingerLower(prices: List<Double>, period: Int, stdDev: Double): Double? {
        return calculateSMA(prices, period)?.let { sma ->
            val variance = prices.takeLast(period).map { (it - sma) * (it - sma) }.average()
            sma - sqrt(variance) * stdDev
        }
    }

    private fun calculateATR(ohlcData: List<OHLC>, period: Int): Double {
        if (ohlcData.size < period) return 0.0
        var tr = 0.0
        for (i in 1 until ohlcData.size) {
            val high = ohlcData[i].high
            val low = ohlcData[i].low
            val prevClose = ohlcData[i - 1].close
            val trueRange = maxOf(high - low, kotlin.math.abs(high - prevClose), kotlin.math.abs(low - prevClose))
            tr += trueRange
        }
        return tr / period
    }
}
