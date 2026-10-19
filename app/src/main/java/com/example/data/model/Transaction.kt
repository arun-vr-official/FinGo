package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val accountId: Long,
    val accountName: String, // Stored to preserve history even if account is deleted
    val toAccountId: Long? = null, // For transfers
    val toAccountName: String? = null,
    val categoryId: Long? = null,
    val categoryName: String = "Uncategorized",
    val categoryIcon: String = "category",
    val categoryColor: String = "#64748B",
    val dateMillis: Long = System.currentTimeMillis(),
    val timeString: String = "",
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val note: String = "",
    val isRefunded: Boolean = false,
    val splitsSummary: String? = null // e.g. "Food: $60.00, Shopping: $40.00"
)
