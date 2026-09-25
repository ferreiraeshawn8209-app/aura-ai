package com.aura.ai.finance.domain.repository

import com.aura.ai.finance.domain.Account
import com.aura.ai.finance.domain.Transaction
import kotlinx.coroutines.flow.Flow

interface IFinanceRepository {
    suspend fun getAccounts(): Result<List<Account>>
    suspend fun getAccount(accountId: String): Result<Account>
    suspend fun getTransactions(accountId: String, limit: Int = 50): Result<List<Transaction>>
    suspend fun getTransactionsByCategory(category: String): Result<List<Transaction>>
    fun watchAccounts(): Flow<List<Account>>
    fun watchTransactions(accountId: String): Flow<List<Transaction>>
}
