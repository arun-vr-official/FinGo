package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WealthType {
    LOAN,
    SAVINGS,
    INVESTMENT,
    INSURANCE;

    fun getDisplayName(): String = when (this) {
        LOAN -> "Loan"
        SAVINGS -> "Savings Goal"
        INVESTMENT -> "Investment"
        INSURANCE -> "Insurance Policy"
    }
}

@Entity(tableName = "wealth_items")
data class WealthItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: WealthType,
    val name: String,
    
    // Amount fields
    val principalOrInitialAmount: Double = 0.0, // Loan principal or Initial Investment
    val currentAmount: Double = 0.0, // Savings current amount or Investment current value
    val targetAmount: Double = 0.0, // Savings target or Loan total
    val outstandingAmount: Double = 0.0, // Loan outstanding
    val contributionOrPaymentAmount: Double = 0.0, // Periodic payment or investment SIP
    
    // Terms & Rates
    val interestRate: Double = 0.0, // %
    val durationMonths: Int = 12,
    val frequency: String = "Monthly", // Weekly, Monthly, Yearly
    
    // Dates
    val startDateMillis: Long = System.currentTimeMillis(),
    val targetOrRenewalDateMillis: Long = System.currentTimeMillis() + 365L * 24 * 3600 * 1000,
    
    // Details
    val subType: String = "", // e.g., "Personal Loan", "Stocks", "Index Fund", "Health", "Life", "Auto"
    val referenceOrPolicyNumber: String = "",
    val notes: String = ""
)
