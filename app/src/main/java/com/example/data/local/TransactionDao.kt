package com.example.data.local

import androidx.room.*
import com.example.data.model.Transaction
import com.example.data.model.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE paymentStatus = 'KASBON_PENDING' ORDER BY timestamp DESC")
    fun getPendingKasbonTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>>

    @Query("UPDATE transactions SET paymentStatus = 'LUNAS' WHERE id = :transactionId")
    suspend fun markAsLunas(transactionId: Long)

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE paymentStatus = 'LUNAS'")
    fun getTotalLunasSales(): Flow<Double?>

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE paymentStatus = 'KASBON_PENDING'")
    fun getTotalPendingKasbon(): Flow<Double?>
}
