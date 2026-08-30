package com.aura.ai.domain.trading.usecase

import com.aura.ai.domain.trading.model.*

/**
 * Paper trading execution use case.
 */
class PaperTradingUseCase(
    private val riskGovernor: RiskGovernorUseCase
) {

    fun createTradePlan(
        signal: TradeSignal,
        portfolioValue: Double,
        riskPercentage: Double = 2.0 // Risk 2% per trade
    ): TradePlan {
        val riskAmount = portfolioValue * (riskPercentage / 100.0)
        val positionSize = kotlin.math.abs(riskAmount / (signal.entryPrice - signal.stopLoss)).toInt().coerceAtLeast(1)
        val potentialProfit = kotlin.math.abs(signal.takeProfit - signal.entryPrice) * positionSize

        return TradePlan(
            symbol = signal.symbol,
            market = signal.market,
            direction = signal.direction,
            entry = signal.entryPrice,
            stopLoss = signal.stopLoss,
            takeProfit = signal.takeProfit,
            positionSize = positionSize,
            riskAmount = riskAmount,
            potentialProfit = potentialProfit,
            riskRewardRatio = signal.riskRewardRatio,
            confidence = signal.confidence,
            signalReason = signal.reason,
            dataStatus = signal.dataStatus
        )
    }
}
