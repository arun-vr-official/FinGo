package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BillFrequency {
    ONE_TIME,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM;

    fun getDisplayName(): String = when (this) {
        ONE_TIME -> "One-time"
        WEEKLY -> "Weekly"
        MONTHLY -> "Monthly"
        YEARLY -> "Yearly"
        CUSTOM -> "Custom"
    }
}

enum class BillStatus {
    PENDING,
    PAID,
    OVERDUE;

    fun getDisplayName(): String = when (this) {
        PENDING -> "Due"
        PAID -> "Paid"
        OVERDUE -> "Overdue"
    }
}

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDateMillis: Long,
    val accountId: Long? = null,
    val accountName: String = "",
    val frequency: BillFrequency = BillFrequency.MONTHLY,
    val reminderEnabled: Boolean = true,
    val status: BillStatus = BillStatus.PENDING,
    val categoryName: String = "Bills",
    val note: String = ""
)
