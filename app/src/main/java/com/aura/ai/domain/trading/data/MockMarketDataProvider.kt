package com.aura.ai.domain.trading.data

import com.aura.ai.domain.trading.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.sin
import kotlin.math.cos
import kotlin.random.Random
import java.time.LocalDateTime

/**
 * Mock market data provider for development and testing.
 * Generates synthetic price data.
 * CLEARLY MARKED AS MOCK - for testing only.
 */
class MockMarketDataProvider : MarketDataProvider {

    private val mockAssets = listOf(
        MarketAsset("NPN", "Nippon Paint Jamaica", "JSE", "EQUITY", "JMD"),
        MarketAsset("NCB", "National Commercial Bank", "JSE", "EQUITY", "JMD"),
        MarketAsset("MSFT", "Microsoft Corporation", "NASDAQ", "EQUITY", "USD"),
        MarketAsset("AAPL", "Apple Inc", "NASDAQ", "EQUITY", "USD"),
        MarketAsset("SPY", "SPDR S&P 500 ETF", "NYSE", "ETF", "USD")
    )

    private val basePrices = mapOf(
        "NPN" to 24.50,
        "NCB" to 68.75,
        "MSFT" to 415.30,
        "AAPL" to 225.50,
        "SPY" to 565.00
    )

    private val random = Random(System.currentTimeMillis())

    override suspend fun getPrice(symbol: String): Result<PriceSnapshot> {
        return try {
            val basePrice = basePrices[symbol] ?: return Result.failure(Exception("Symbol not found in mock data"))
            val price = generateMockPrice(basePrice)
            Result.success(
                PriceSnapshot(
                    symbol = symbol,
                    price = price,
                    timestamp = LocalDateTime.now(),
                    status = PriceStatus.MOCK,
                    open = price * 0.98,
                    high = price * 1.02,
                    low = price * 0.96,
                    close = price,
                    volume = random.nextLong(1_000_000L, 10_000_000L)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPrices(symbols: List<String>): Result<List<PriceSnapshot>> {
        return try {
            val prices = symbols.mapNotNull { symbol ->
                basePrices[symbol]?.let { basePrice ->
                    val price = generateMockPrice(basePrice)
                    PriceSnapshot(
                        symbol = symbol,
                        price = price,
                        timestamp = LocalDateTime.now(),
                        status = PriceStatus.MOCK,
                        volume = random.nextLong(1_000_000L, 10_000_000L)
                    )
                }
            }
            Result.success(prices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOHLC(symbol: String, timeframe: Timeframe, limit: Int): Result<List<OHLC>> {
        return try {
            val basePrice = basePrices[symbol] ?: return Result.failure(Exception("Symbol not found"))
            val ohlcList = (0 until limit).map { i ->
                val offset = (i * 0.5).toInt() % 360
                val price = basePrice * (1.0 + sin(Math.toRadians(offset.toDouble())) * 0.1)
                OHLC(
                    symbol = symbol,
                    timeframe = timeframe,
                    open = price * 0.98,
                    high = price * 1.02,
                    low = price * 0.96,
                    close = price,
                    volume = random.nextLong(1_000_000L, 10_000_000L),
                    timestamp = LocalDateTime.now().minusHours((limit - i).toLong())
                )
            }
            Result.success(ohlcList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAsset(symbol: String): Result<MarketAsset> {
        val asset = mockAssets.find { it.symbol == symbol }
        return if (asset != null) Result.success(asset) else Result.failure(Exception("Asset not found"))
    }

    override suspend fun searchAssets(query: String, market: String): Result<List<MarketAsset>> {
        val filtered = mockAssets.filter {
            it.symbol.contains(query, ignoreCase = true) ||
            it.name.contains(query, ignoreCase = true)
        }.let { assets ->
            if (market.isNotEmpty()) assets.filter { it.market == market } else assets
        }
        return Result.success(filtered)
    }

    override fun streamPrices(symbols: List<String>): Flow<PriceSnapshot> {
        return flowOf() // Mock doesn't stream; return empty flow
    }

    override suspend fun getStatus(): MarketDataStatus {
        return MarketDataStatus(
            isAvailable = true,
            provider = "MOCK (Development Only)",
            rateLimitRemaining = -1
        )
    }

    private fun generateMockPrice(basePrice: Double): Double {
        val variance = random.nextDouble(-2.0, 2.0) // ±2%
        return basePrice * (1.0 + variance / 100.0)
    }
}
