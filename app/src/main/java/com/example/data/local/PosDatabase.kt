package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Transaction::class,
        TransactionItem::class,
        CustomerDebt::class,
        Employee::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDebtDao(): CustomerDebtDao
    abstract fun employeeDao(): EmployeeDao

    companion object {
        @Volatile
        private var INSTANCE: PosDatabase? = null

        fun getDatabase(context: Context): PosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PosDatabase::class.java,
                    "kasirku_pos_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial UMKM sample catalog & employee roles
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDatabase(database)
                    }
                }
            }
        }

        private suspend fun seedDatabase(db: PosDatabase) {
            // Pre-populate realistic UMKM Products matching reference UI (Food, Beverage, Grocery)
            val sampleProducts = listOf(
                Product(name = "Cheeseburger Special", barcode = "8991001001", category = "Burger", buyPrice = 25000.0, sellPrice = 35000.0, stock = 24, imageUrl = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400"),
                Product(name = "Fresh Fuji Orange Juice", barcode = "8991001002", category = "Minuman", buyPrice = 8000.0, sellPrice = 12500.0, stock = 18, imageUrl = "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=400"),
                Product(name = "Garden Fresh Salad", barcode = "8991001003", category = "Snacks", buyPrice = 11000.0, sellPrice = 17000.0, stock = 12, imageUrl = "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400"),
                Product(name = "Organic Strawberry Smoothie", barcode = "8991001004", category = "Shakes", buyPrice = 5000.0, sellPrice = 7500.0, stock = 8, imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?w=400"),
                Product(name = "Sweet Bakery Croissant", barcode = "8991001005", category = "Bakery", buyPrice = 9000.0, sellPrice = 15000.0, stock = 3, minStockAlert = 5, imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400"),
                Product(name = "Kopi Susu Gula Aren", barcode = "8991001006", category = "Minuman", buyPrice = 10000.0, sellPrice = 18000.0, stock = 35, imageUrl = "https://images.unsplash.com/photo-1541167760496-1628856ab772?w=400"),
                Product(name = "Nasi Goreng Spesial UMKM", barcode = "8991001007", category = "Makanan", buyPrice = 12000.0, sellPrice = 22000.0, stock = 40, imageUrl = "https://images.unsplash.com/photo-1603133872878-684f208fb84b?w=400"),
                Product(name = "Keripik Singkong Renyah", barcode = "8991001008", category = "Snacks", buyPrice = 6000.0, sellPrice = 10000.0, stock = 2, minStockAlert = 5, imageUrl = "https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400")
            )
            db.productDao().insertAll(sampleProducts)

            // Pre-populate Employees / Roles
            val sampleEmployees = listOf(
                Employee(name = "M. A. Rouf (Owner)", role = EmployeeRole.OWNER, pin = "8888", phone = "081234567890"),
                Employee(name = "Budi Santoso (Kasir)", role = EmployeeRole.CASHIER, pin = "1234", phone = "081298765432"),
                Employee(name = "Siti Rahma (Manager)", role = EmployeeRole.MANAGER, pin = "5555", phone = "081311223344")
            )
            db.employeeDao().insertAll(sampleEmployees)

            // Seed initial sample customer debts for UMKM kasbon demonstration
            val now = System.currentTimeMillis()
            val dayInMillis = 86400000L
            val sampleDebts = listOf(
                CustomerDebt(customerName = "M A Rouf", phone = "08123456789", totalDebt = 45000.0, paidAmount = 0.0, dueDateTimestamp = now + (2 * dayInMillis), notes = "Kasbon 2x Cheeseburger"),
                CustomerDebt(customerName = "Ibu Halimah", phone = "08569876543", totalDebt = 120000.0, paidAmount = 50000.0, dueDateTimestamp = now - (1 * dayInMillis), notes = "Sembako & Kopi"),
                CustomerDebt(customerName = "Pak Pakusadewo", phone = "08180987654", totalDebt = 85000.0, paidAmount = 0.0, dueDateTimestamp = now + (5 * dayInMillis), notes = "Sarapan Nasi Goreng")
            )
            sampleDebts.forEach { db.customerDebtDao().insertDebt(it) }

            // Seed sample completed sales transaction for analytics dashboard
            val sampleTx = Transaction(
                invoiceNumber = "INV-20260811-001",
                timestamp = now - 3600000L,
                totalAmount = 45000.0,
                paymentMode = PaymentMode.CASH,
                paymentStatus = PaymentStatus.LUNAS,
                customerName = "Pelanggan Umum",
                cashierName = "Budi Santoso (Kasir)"
            )
            val txId = db.transactionDao().insertTransaction(sampleTx)
            db.transactionDao().insertTransactionItems(
                listOf(
                    TransactionItem(transactionId = txId, productId = 1, productName = "Cheeseburger Special", quantity = 1, pricePerUnit = 35000.0, totalPrice = 35000.0),
                    TransactionItem(transactionId = txId, productId = 8, productName = "Keripik Singkong Renyah", quantity = 1, pricePerUnit = 10000.0, totalPrice = 10000.0)
                )
            )
        }
    }
}
