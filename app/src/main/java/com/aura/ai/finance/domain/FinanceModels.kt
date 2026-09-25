package com.aura.ai.finance.domain

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: String, // CHECKING, SAVINGS, CREDIT, INVESTMENT
    val balance: Double,
    val currency: String = "USD",
    val lastUpdated: Long
)

@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    val description: String,
    val amount: Double,
    val category: String,
    val timestamp: Long,
    val status: String // PENDING, COMPLETED, FAILED
)

@Serializable
data class TransactionCategory(
    val name: String,
    val icon: String? = null,
    val color: String? = null
)

data class FinanceState {
    val totalBalance: Double? = null
    val accounts: List<Account> = emptyList()
    val transactions: List<Transaction> = emptyList()
    val error: String? = null
    val isLoading: Boolean = false
}
