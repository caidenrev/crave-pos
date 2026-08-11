package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class PosRepository(private val database: PosDatabase) {
    val allProducts: Flow<List<Product>> = database.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = database.productDao().getLowStockProducts()
    val allTransactions: Flow<List<Transaction>> = database.transactionDao().getAllTransactions()
    val pendingKasbonTransactions: Flow<List<Transaction>> = database.transactionDao().getPendingKasbonTransactions()
    val allCustomerDebts: Flow<List<CustomerDebt>> = database.customerDebtDao().getAllDebts()
    val unsettledDebts: Flow<List<CustomerDebt>> = database.customerDebtDao().getUnsettledDebts()
    val allEmployees: Flow<List<Employee>> = database.employeeDao().getAllEmployees()

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return if (category == "All" || category.isEmpty()) {
            allProducts
        } else {
            database.productDao().getProductsByCategory(category)
        }
    }

    suspend fun getProductByBarcode(barcode: String): Product? =
        database.productDao().getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long =
        database.productDao().insertProduct(product)

    suspend fun updateProduct(product: Product) =
        database.productDao().updateProduct(product)

    suspend fun deleteProduct(product: Product) =
        database.productDao().deleteProduct(product)

    suspend fun addStock(productId: Long, quantity: Int) =
        database.productDao().addStock(productId, quantity)

    suspend fun createTransaction(
        transaction: Transaction,
        cartItems: List<CartItem>
    ): Long {
        val txId = database.transactionDao().insertTransaction(transaction)
        val txItems = cartItems.map { cart ->
            TransactionItem(
                transactionId = txId,
                productId = cart.product.id,
                productName = cart.product.name,
                quantity = cart.quantity,
                pricePerUnit = cart.product.sellPrice,
                totalPrice = cart.subtotal
            )
        }
        database.transactionDao().insertTransactionItems(txItems)

        // Decrease stock automatically
        cartItems.forEach { cart ->
            database.productDao().decreaseStock(cart.product.id, cart.quantity)
        }

        // If payment mode is KASBON, record in CustomerDebt table too
        if (transaction.paymentMode == PaymentMode.KASBON) {
            val debt = CustomerDebt(
                customerName = transaction.customerName,
                phone = transaction.customerPhone.ifEmpty { "-" },
                totalDebt = transaction.totalAmount,
                paidAmount = 0.0,
                dueDateTimestamp = System.currentTimeMillis() + (7 * 86400000L), // 7 days default
                notes = "Kasbon Inv #${transaction.invoiceNumber}"
            )
            database.customerDebtDao().insertDebt(debt)
        }

        return txId
    }

    fun getTransactionItems(transactionId: Long): Flow<List<TransactionItem>> =
        database.transactionDao().getItemsForTransaction(transactionId)

    suspend fun markTransactionAsLunas(transactionId: Long) =
        database.transactionDao().markAsLunas(transactionId)

    suspend fun recordDebtPayment(debtId: Long, amount: Double) =
        database.customerDebtDao().recordPayment(debtId, amount)

    suspend fun insertCustomerDebt(debt: CustomerDebt) =
        database.customerDebtDao().insertDebt(debt)

    suspend fun updateCustomerDebt(debt: CustomerDebt) =
        database.customerDebtDao().updateDebt(debt)

    suspend fun deleteCustomerDebt(debt: CustomerDebt) =
        database.customerDebtDao().deleteDebt(debt)

    suspend fun insertEmployee(employee: Employee) =
        database.employeeDao().insertEmployee(employee)

    suspend fun updateEmployee(employee: Employee) =
        database.employeeDao().updateEmployee(employee)

    suspend fun deleteEmployee(employee: Employee) =
        database.employeeDao().deleteEmployee(employee)
}
