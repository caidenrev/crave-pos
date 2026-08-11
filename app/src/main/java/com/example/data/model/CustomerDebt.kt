package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customer_debts")
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerName: String,
    val phone: String,
    val totalDebt: Double,
    val paidAmount: Double = 0.0,
    val dueDateTimestamp: Long,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val isSettled: Boolean = false,
    val notes: String = ""
) {
    val remainingDebt: Double
        get() = (totalDebt - paidAmount).coerceAtLeast(0.0)
}
