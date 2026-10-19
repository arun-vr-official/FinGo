package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    BANK,
    CREDIT_CARD,
    DEBIT_CARD,
    CASH,
    WALLET,
    OTHER;

    fun getDisplayName(): String = when (this) {
        BANK -> "Bank Account"
        CREDIT_CARD -> "Credit Card"
        DEBIT_CARD -> "Debit Card"
        CASH -> "Cash"
        WALLET -> "Digital Wallet"
        OTHER -> "Other"
    }
}

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val providerName: String = "",
    val last4Digits: String = "",
    val type: AccountType = AccountType.BANK,
    val creditLimit: Double = 0.0,
    val colorHex: String = "#10B981",
    val iconName: String = "account_balance",
    val balance: Double = 0.0, // Current balance or outstanding balance for credit cards
    val billingDate: Int = 1, // Day of month
    val paymentDueDate: Int = 15,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
