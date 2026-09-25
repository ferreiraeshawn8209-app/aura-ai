package com.aura.ai.trading.domain

import kotlinx.serialization.Serializable

@Serializable
data class MarketData(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercent: Double,
    val high: Double,
    val low: Double,
    val volume: Long,
    val timestamp: Long
)

@Serializable
data class TradingSignal(
    val symbol: String,
    val signal: String, // BUY, SELL, HOLD
    val strength: Int, // 1-10 scale
    val reason: String,
    val confidence: Double, // 0.0-1.0
    val timestamp: Long
)

@Serializable
data class Position(
    val id: String,
    val symbol: String,
    val quantity: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double? = null,
    val takeProfit: Double? = null,
    val pnl: Double = (currentPrice - entryPrice) * quantity,
    val pnlPercent: Double = ((currentPrice - entryPrice) / entryPrice) * 100
)

@Serializable
data class Order(
    val id: String,
    val symbol: String,
    val type: String, // MARKET, LIMIT, STOP
    val side: String, // BUY, SELL
    val quantity: Double,
    val price: Double? = null,
    val stopPrice: Double? = null,
    val status: String, // PENDING, FILLED, CANCELLED, REJECTED
    val timestamp: Long
)

@Serializable
data class RiskSettings(
    val maxPositionSize: Double = 0.05, // 5% max per position
    val maxDrawdown: Double = 0.20, // 20% max loss
    val dailyLossLimit: Double = 0.10, // 10% daily loss limit
    val riskPerTrade: Double = 0.02 // 2% risk per trade
)
