package com.aura.ai.trading.domain.repository

import com.aura.ai.trading.domain.MarketData
import com.aura.ai.trading.domain.Order
import com.aura.ai.trading.domain.Position
import com.aura.ai.trading.domain.TradingSignal
import kotlinx.coroutines.flow.Flow

interface ITradingRepository {
    suspend fun getMarketData(symbol: String): Result<MarketData>
    suspend fun getPositions(): Result<List<Position>>
    suspend fun getPosition(symbol: String): Result<Position>
    suspend fun placeOrder(order: Order): Result<String> // Returns order ID
    suspend fun cancelOrder(orderId: String): Result<Boolean>
    suspend fun getOrderHistory(limit: Int = 50): Result<List<Order>>
    fun watchMarketData(symbol: String): Flow<MarketData>
    fun watchPositions(): Flow<List<Position>>
}
