package com.aura.ai.domain.trading.usecase

import com.aura.ai.domain.trading.model.*

/**
 * Risk Governor: Validates trades against configured limits.
 * Acts as safety layer between AI and execution.
 */
class RiskGovernorUseCase {

    fun validateTradePlan(
        plan: TradePlan,
        portfolio: Portfolio,
        riskSettings: RiskGovernor,
        tradingStatus: TradingStatus
    ): ValidationResult {
        // Check if trading is halted
        if (tradingStatus.isTradingHalted) {
            return ValidationResult(false, "Trading is currently halted: ${tradingStatus.haltReason}")
        }

        // Check AURA score
        if (plan.confidence * 100 < riskSettings.minimumAuraScore) {
            return ValidationResult(false, "AURA score (${(plan.confidence * 100).toInt()}) below minimum (${riskSettings.minimumAuraScore})")
        }

        // Check mandatory stop loss
        if (riskSettings.mandatoryStopLoss && plan.stopLoss == 0.0) {
            return ValidationResult(false, "Mandatory stop loss required")
        }

        // Check max risk per trade
        val riskAmount = kotlin.math.abs(plan.entry - plan.stopLoss) * plan.positionSize
        if (riskAmount > riskSettings.maxRiskPerTrade) {
            return ValidationResult(false, "Risk per trade (R${riskAmount}) exceeds limit (R${riskSettings.maxRiskPerTrade})")
        }

        // Check max daily loss
        if (tradingStatus.dailyLossIncurred + riskAmount > riskSettings.maxDailyLoss) {
            return ValidationResult(false, "Daily loss limit would be exceeded")
        }

        // Check open positions limit
        if (portfolio.positions.size >= riskSettings.maxOpenPositions) {
            return ValidationResult(false, "Maximum open positions (${riskSettings.maxOpenPositions}) reached")
        }

        // Check trades per day
        if (tradingStatus.tradesExecutedToday >= riskSettings.maxTradesPerDay) {
            return ValidationResult(false, "Maximum trades per day (${riskSettings.maxTradesPerDay}) reached")
        }

        // Check position size limit
        if (plan.positionSize > riskSettings.maxPositionSize) {
            return ValidationResult(false, "Position size (${plan.positionSize}) exceeds maximum (${riskSettings.maxPositionSize})")
        }

        // Check portfolio exposure
        val portfolioValue = portfolio.calculateTotalPortfolioValue()
        val totalExposure = portfolio.positions.sumOf { it.quantity * it.currentPrice } + riskAmount
        if (totalExposure > portfolioValue * riskSettings.maxPortfolioExposure) {
            return ValidationResult(false, "Portfolio exposure would exceed ${riskSettings.maxPortfolioExposure * 100}%")
        }

        return ValidationResult(true, "Trade plan validated")
    }

    fun checkTradingHalt(
        dailyLossIncurred: Double,
        startingBalance: Double,
        riskSettings: RiskGovernor
    ): Boolean {
        val drawdownPercent = (dailyLossIncurred / startingBalance)
        return drawdownPercent >= riskSettings.tradingHaltThreshold
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)
