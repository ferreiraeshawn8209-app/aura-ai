package com.aura.ai.domain.trading.data

import com.aura.ai.domain.trading.model.*

/**
 * Abstract broker interface for trading execution.
 * Implementations: PaperTradingBroker, IB Gateway, Regulated Broker APIs
 */
interface BrokerGateway {
    suspend fun getAccount(): Result<AccountInfo>
    suspend fun getPositions(): Result<List<Position>>
    suspend fun getBuyingPower(): Result<Double>
    suspend fun placeOrder(order: PaperOrder): Result<PaperOrder>
    suspend fun cancelOrder(orderId: String): Result<Boolean>
    suspend fun getOrderStatus(orderId: String): Result<OrderStatus>
    suspend fun getTradeHistory(limit: Int = 100): Result<List<ExecutedTrade>>
}

data class AccountInfo(
    val accountId: String,
    val balance: Double,
    val buyingPower: Double,
    val investedCapital: Double,
    val unrealisedPnL: Double,
    val realisedPnL: Double
)

data class ExecutedTrade(
    val id: String,
    val symbol: String,
    val direction: TradeDirection,
    val quantity: Int,
    val entryPrice: Double,
    val exitPrice: Double? = null,
    val pnL: Double? = null,
    val pnLPercent: Double? = null,
    val enteredAt: Long,
    val exitedAt: Long? = null
)
