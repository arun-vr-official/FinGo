package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthKey: String, // "YYYY-MM" or "OVERALL"
    val categoryId: Long? = null, // null means overall spending budget for the month
    val categoryName: String = "Overall Spending",
    val limitAmount: Double = 0.0,
    val colorHex: String = "#3B82F6"
)
