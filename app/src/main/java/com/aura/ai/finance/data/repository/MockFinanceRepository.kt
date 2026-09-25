package com.aura.ai.finance.data.repository

import com.aura.ai.finance.domain.Account
import com.aura.ai.finance.domain.Transaction
import com.aura.ai.finance.domain.repository.IFinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class MockFinanceRepository @Inject constructor() : IFinanceRepository {
    override suspend fun getAccounts(): Result<List<Account>> {
        return Result.success(
            listOf(
                Account(
                    id = "acc_001",
                    name = "Checking",
                    type = "CHECKING",
                    balance = 2500.00,
                    lastUpdated = System.currentTimeMillis()
                ),
                Account(
                    id = "acc_002",
                    name = "Savings",
                    type = "SAVINGS",
                    balance = 15000.00,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        )
    }

    override suspend fun getAccount(accountId: String): Result<Account> {
        return Result.failure(Exception("Real finance provider not configured"))
    }

    override suspend fun getTransactions(accountId: String, limit: Int): Result<List<Transaction>> {
        return Result.failure(Exception("Real finance provider not configured"))
    }

    override suspend fun getTransactionsByCategory(category: String): Result<List<Transaction>> {
        return Result.failure(Exception("Real finance provider not configured"))
    }

    override fun watchAccounts(): Flow<List<Account>> = flowOf(emptyList())

    override fun watchTransactions(accountId: String): Flow<List<Transaction>> = flowOf(emptyList())
}
