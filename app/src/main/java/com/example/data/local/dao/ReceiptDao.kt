package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE id = :id LIMIT 1")
    suspend fun getReceiptById(id: Long): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE receiptNumber = :receiptNumber LIMIT 1")
    suspend fun getReceiptByNumber(receiptNumber: String): ReceiptEntity?

    @Query("SELECT * FROM receipts WHERE receiptNumber LIKE '%' || :query || '%' OR customerName LIKE '%' || :query || '%' OR itemsJson LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchReceipts(query: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE createdAt >= :startTimestamp AND createdAt <= :endTimestamp ORDER BY createdAt DESC")
    fun getReceiptsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<ReceiptEntity>>

    @Query("SELECT COUNT(*) FROM receipts")
    fun getTotalReceiptsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM receipts WHERE createdAt >= :startOfDayTimestamp")
    fun getTodayReceiptsCount(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM receipts WHERE createdAt >= :startOfDayTimestamp")
    fun getTodayTotalSales(startOfDayTimestamp: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalAmount), 0.0) FROM receipts")
    fun getTotalSales(): Flow<Double>

    @Query("SELECT * FROM receipts ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentReceipts(limit: Int = 5): Flow<List<ReceiptEntity>>

    @Query("SELECT MAX(sequenceNumber) FROM receipts")
    suspend fun getMaxSequenceNumber(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceiptById(id: Long)

    @Query("DELETE FROM receipts")
    suspend fun clearAllReceipts()
}
