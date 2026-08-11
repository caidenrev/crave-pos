package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMode {
    CASH, QRIS, BANK_TRANSFER, EWALLET, KASBON
}

enum class PaymentStatus {
    LUNAS, KASBON_PENDING, PARTIALLY_PAID
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val paymentMode: PaymentMode,
    val paymentStatus: PaymentStatus,
    val customerName: String = "Pelanggan Umum",
    val customerPhone: String = "",
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val extraCharges: Double = 0.0,
    val cashierName: String = "Kasir 1",
    val notes: String = ""
)

@Entity(tableName = "transaction_items")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val pricePerUnit: Double,
    val totalPrice: Double
)
