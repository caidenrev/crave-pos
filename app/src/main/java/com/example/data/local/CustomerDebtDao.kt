package com.example.data.local

import androidx.room.*
import com.example.data.model.CustomerDebt
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDebtDao {
    @Query("SELECT * FROM customer_debts ORDER BY dueDateTimestamp ASC")
    fun getAllDebts(): Flow<List<CustomerDebt>>

    @Query("SELECT * FROM customer_debts WHERE isSettled = 0 ORDER BY dueDateTimestamp ASC")
    fun getUnsettledDebts(): Flow<List<CustomerDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: CustomerDebt): Long

    @Update
    suspend fun updateDebt(debt: CustomerDebt)

    @Query("UPDATE customer_debts SET paidAmount = paidAmount + :amount, isSettled = (paidAmount + :amount >= totalDebt), lastUpdatedTimestamp = :now WHERE id = :debtId")
    suspend fun recordPayment(debtId: Long, amount: Double, now: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDebt(debt: CustomerDebt)
}
