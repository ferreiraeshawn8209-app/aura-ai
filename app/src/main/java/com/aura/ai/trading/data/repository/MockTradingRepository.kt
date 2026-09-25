package com.aura.ai.trading.data.repository

import com.aura.ai.trading.domain.MarketData
import com.aura.ai.trading.domain.Order
import com.aura.ai.trading.domain.Position
import com.aura.ai.trading.domain.repository.ITradingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class MockTradingRepository @Inject constructor() : ITradingRepository {
    override suspend fun getMarketData(symbol: String): Result<MarketData> {
        return Result.failure(Exception("Real trading provider not configured"))
    }

    override suspend fun getPositions(): Result<List<Position>> {
        return Result.failure(Exception("Real trading provider not configured"))
    }

    override suspend fun getPosition(symbol: String): Result<Position> {
        return Result.failure(Exception("Real trading provider not configured"))
    }

    override suspend fun placeOrder(order: Order): Result<String> {
        return Result.failure(Exception("Order execution requires real trading provider configuration"))
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        return Result.failure(Exception("Real trading provider not configured"))
    }

    override suspend fun getOrderHistory(limit: Int): Result<List<Order>> {
        return Result.failure(Exception("Real trading provider not configured"))
    }

    override fun watchMarketData(symbol: String): Flow<MarketData> = flowOf()

    override fun watchPositions(): Flow<List<Position>> = flowOf()
}
