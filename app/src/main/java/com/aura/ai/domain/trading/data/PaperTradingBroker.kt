package com.aura.ai.domain.trading.data

import com.aura.ai.domain.trading.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import kotlin.math.abs

/**
 * In-memory paper trading broker for virtual trades using real market prices.
 * No real money is exchanged; all trades are simulated.
 */
class PaperTradingBroker(
    private val initialBalance: Double,
    private val marketDataProvider: MarketDataProvider,
    private val slippagePercent: Double = 0.1 // 0.1% slippage
) : BrokerGateway {

    private val mutex = Mutex()
    private var currentBalance = initialBalance
    private var investedCapital = 0.0
    private val positions = mutableMapOf<String, Position>()
    private val executedTrades = mutableListOf<ExecutedTrade>()
    private val orders = mutableMapOf<String, PaperOrder>()
    private val positionHistory = mutableListOf<ClosedPosition>()

    override suspend fun getAccount(): Result<AccountInfo> = mutex.withLock {
        val unrealisedPnL = calculateUnrealisedPnL()
        val realisedPnL = positionHistory.sumOf { it.pnL }
        return Result.success(
            AccountInfo(
                accountId = "PAPER_TRADING",
                balance = currentBalance,
                buyingPower = currentBalance,
                investedCapital = investedCapital,
                unrealisedPnL = unrealisedPnL,
                realisedPnL = realisedPnL
            )
        )
    }

    override suspend fun getPositions(): Result<List<Position>> = mutex.withLock {
        return Result.success(positions.values.toList())
    }

    override suspend fun getBuyingPower(): Result<Double> = mutex.withLock {
        return Result.success(currentBalance)
    }

    override suspend fun placeOrder(order: PaperOrder): Result<PaperOrder> = mutex.withLock {
        try {
            // Get current price
            val priceResult = marketDataProvider.getPrice(order.symbol)
            if (priceResult.isFailure) {
                return Result.failure(priceResult.exceptionOrNull() ?: Exception("Failed to get price"))
            }

            val priceSnapshot = priceResult.getOrNull()!!
            val executionPrice = applySlippage(priceSnapshot.price, order.direction)
            val totalCost = executionPrice * order.quantity

            return when {
                order.direction == TradeDirection.BUY && totalCost > currentBalance -> {
                    Result.failure(Exception("Insufficient buying power. Need: $totalCost, Available: $currentBalance"))
                }
                order.direction == TradeDirection.SELL && !positions.containsKey(order.symbol) -> {
                    Result.failure(Exception("No position to sell for ${order.symbol}"))
                }
                else -> {
                    val executedOrder = order.copy(
                        id = UUID.randomUUID().toString(),
                        status = OrderStatus.EXECUTED,
                        executedAt = java.time.LocalDateTime.now(),
                        executionPrice = executionPrice
                    )

                    when (order.direction) {
                        TradeDirection.BUY -> executeBuy(executedOrder, executionPrice)
                        TradeDirection.SELL -> executeSell(executedOrder, executionPrice)
                    }

                    orders[executedOrder.id] = executedOrder
                    Result.success(executedOrder)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> = mutex.withLock {
        val order = orders[orderId] ?: return Result.failure(Exception("Order not found"))
        return if (order.status == OrderStatus.PENDING) {
            orders[orderId] = order.copy(status = OrderStatus.CANCELLED)
            Result.success(true)
        } else {
            Result.failure(Exception("Can only cancel pending orders"))
        }
    }

    override suspend fun getOrderStatus(orderId: String): Result<OrderStatus> = mutex.withLock {
        val status = orders[orderId]?.status ?: return Result.failure(Exception("Order not found"))
        return Result.success(status)
    }

    override suspend fun getTradeHistory(limit: Int): Result<List<ExecutedTrade>> = mutex.withLock {
        return Result.success(executedTrades.takeLast(limit))
    }

    // ========== PRIVATE HELPERS ==========

    private suspend fun executeBuy(order: PaperOrder, executionPrice: Double) {
        val totalCost = executionPrice * order.quantity
        currentBalance -= totalCost
        investedCapital += totalCost

        val existingPosition = positions[order.symbol]
        val newPosition = if (existingPosition != null) {
            // Average up
            val totalQuantity = existingPosition.quantity + order.quantity
            val newEntryPrice = (
                (existingPosition.entryPrice * existingPosition.quantity) +
                (executionPrice * order.quantity)
            ) / totalQuantity
            existingPosition.copy(
                quantity = totalQuantity,
                entryPrice = newEntryPrice,
                currentPrice = executionPrice
            )
        } else {
            Position(
                id = UUID.randomUUID().toString(),
                symbol = order.symbol,
                quantity = order.quantity,
                entryPrice = executionPrice,
                currentPrice = executionPrice,
                stopLoss = 0.0,
                takeProfit = 0.0
            )
        }
        positions[order.symbol] = newPosition
    }

    private suspend fun executeSell(order: PaperOrder, executionPrice: Double) {
        val position = positions[order.symbol] ?: return
        val proceeds = executionPrice * order.quantity
        currentBalance += proceeds
        investedCapital -= (position.entryPrice * order.quantity)

        val pnL = (executionPrice - position.entryPrice) * order.quantity
        val pnLPercent = ((executionPrice - position.entryPrice) / position.entryPrice) * 100

        executedTrades.add(
            ExecutedTrade(
                id = UUID.randomUUID().toString(),
                symbol = order.symbol,
                direction = TradeDirection.SELL,
                quantity = order.quantity,
                entryPrice = position.entryPrice,
                exitPrice = executionPrice,
                pnL = pnL,
                pnLPercent = pnLPercent,
                enteredAt = position.openedAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                exitedAt = System.currentTimeMillis()
            )
        )

        positionHistory.add(
            ClosedPosition(
                symbol = order.symbol,
                entryPrice = position.entryPrice,
                exitPrice = executionPrice,
                quantity = order.quantity,
                pnL = pnL
            )
        )

        if (order.quantity >= position.quantity) {
            positions.remove(order.symbol)
        } else {
            val remainingQuantity = position.quantity - order.quantity
            positions[order.symbol] = position.copy(quantity = remainingQuantity)
        }
    }

    private fun applySlippage(price: Double, direction: TradeDirection): Double {
        val slippage = price * (slippagePercent / 100.0)
        return when (direction) {
            TradeDirection.BUY -> price + slippage
            TradeDirection.SELL -> price - slippage
        }
    }

    private fun calculateUnrealisedPnL(): Double {
        return positions.values.sumOf { position ->
            (position.currentPrice - position.entryPrice) * position.quantity
        }
    }

    suspend fun updatePositionPrice(symbol: String, currentPrice: Double) = mutex.withLock {
        val position = positions[symbol] ?: return@withLock
        positions[symbol] = position.copy(
            currentPrice = currentPrice,
            unrealisedPnL = position.calculateUnrealisedPnL(currentPrice),
            unrealisedPnLPercent = position.calculateUnrealisedPnLPercent(currentPrice)
        )
    }

    suspend fun getPortfolioSnapshot(currentPrices: Map<String, Double>): Portfolio {
        return mutex.withLock {
            val unrealisedPnL = positions.values.sumOf { position ->
                val price = currentPrices[position.symbol] ?: position.currentPrice
                (price - position.entryPrice) * position.quantity
            }
            val realisedPnL = positionHistory.sumOf { it.pnL }
            val totalPnL = unrealisedPnL + realisedPnL
            val portfolioValue = currentBalance + unrealisedPnL
            val totalReturn = portfolioValue - initialBalance
            val totalReturnPercent = (totalReturn / initialBalance) * 100

            Portfolio(
                id = "PAPER",
                startingBalance = initialBalance,
                currentBalance = currentBalance,
                availableCash = currentBalance,
                positions = positions.values.toList(),
                totalUnrealisedPnL = unrealisedPnL,
                totalRealisedPnL = realisedPnL,
                totalReturn = totalReturn,
                totalReturnPercent = totalReturnPercent,
                numberOfTrades = executedTrades.size,
                winRate = if (executedTrades.isNotEmpty()) {
                    executedTrades.count { it.pnL!! > 0 }.toDouble() / executedTrades.size
                } else 0.0
            )
        }
    }
}

data class ClosedPosition(
    val symbol: String,
    val entryPrice: Double,
    val exitPrice: Double,
    val quantity: Int,
    val pnL: Double
)
