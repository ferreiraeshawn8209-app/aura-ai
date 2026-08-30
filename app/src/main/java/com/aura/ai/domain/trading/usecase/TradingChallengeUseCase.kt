package com.aura.ai.domain.trading.usecase

import com.aura.ai.domain.trading.model.*

/**
 * Calculates challenge progress and metrics.
 */
class TradingChallengeUseCase {

    fun calculateChallengeProgress(
        challenge: TradingChallenge,
        currentPortfolioValue: Double
    ): ChallengeProgress {
        val daysElapsed = challenge.getDaysElapsed()
        val daysRemaining = challenge.getDaysRemaining()
        val requiredTotalReturn = challenge.getRequiredTotalReturn()
        val requiredTotalReturnPercent = challenge.getRequiredTotalReturnPercent()
        val requiredCAGR = challenge.getRequiredCAGR()
        val currentReturn = currentPortfolioValue - challenge.startingCapital
        val currentReturnPercent = (currentReturn / challenge.startingCapital) * 100
        val progressPercent = challenge.getProgressPercent()
        val remainingAmount = challenge.getRemainingAmount()
        val isAheadOfSchedule = challenge.isAheadOfSchedule()

        // Calculate if on track
        val requiredProgressAtThisPoint = (daysElapsed.toDouble() / challenge.durationDays) * 100
        val isOnTrack = progressPercent >= requiredProgressAtThisPoint

        return ChallengeProgress(
            daysElapsed = daysElapsed,
            daysRemaining = daysRemaining,
            requiredTotalReturn = requiredTotalReturn,
            requiredTotalReturnPercent = requiredTotalReturnPercent,
            requiredCAGR = requiredCAGR,
            currentReturn = currentReturn,
            currentReturnPercent = currentReturnPercent,
            progressPercent = progressPercent,
            remainingAmount = remainingAmount,
            isAheadOfSchedule = isAheadOfSchedule,
            isOnTrack = isOnTrack,
            requiredDailyReturn = requiredTotalReturnPercent / challenge.durationDays,
            currentDailyReturn = currentReturnPercent / (daysElapsed + 1).toDouble()
        )
    }
}

data class ChallengeProgress(
    val daysElapsed: Long,
    val daysRemaining: Long,
    val requiredTotalReturn: Double,
    val requiredTotalReturnPercent: Double,
    val requiredCAGR: Double,
    val currentReturn: Double,
    val currentReturnPercent: Double,
    val progressPercent: Double,
    val remainingAmount: Double,
    val isAheadOfSchedule: Boolean,
    val isOnTrack: Boolean,
    val requiredDailyReturn: Double,
    val currentDailyReturn: Double
)
