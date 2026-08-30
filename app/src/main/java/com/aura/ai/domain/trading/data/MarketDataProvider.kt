package com.aura.ai.domain.trading.data

import com.aura.ai.domain.trading.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Abstract interface for market data providers.
 * Implementations can be real APIs (Alpha Vantage, IB API) or mock providers.
 */
interface MarketDataProvider {
    suspend fun getPrice(symbol: String): Result<PriceSnapshot>
    suspend fun getPrices(symbols: List<String>): Result<List<PriceSnapshot>>
    suspend fun getOHLC(symbol: String, timeframe: Timeframe, limit: Int = 100): Result<List<OHLC>>
    suspend fun getAsset(symbol: String): Result<MarketAsset>
    suspend fun searchAssets(query: String, market: String = ""): Result<List<MarketAsset>>
    fun streamPrices(symbols: List<String>): Flow<PriceSnapshot>
    suspend fun getStatus(): MarketDataStatus
}

data class MarketDataStatus(
    val isAvailable: Boolean,
    val lastUpdate: Long = System.currentTimeMillis(),
    val rateLimitRemaining: Int = -1,
    val provider: String = "UNKNOWN"
)
