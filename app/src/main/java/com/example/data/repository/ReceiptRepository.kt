package com.example.data.repository

import com.example.data.local.dao.ReceiptDao
import com.example.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ReceiptRepository(private val receiptDao: ReceiptDao) {
    val allReceipts: Flow<List<ReceiptEntity>> = receiptDao.getAllReceipts()
    val totalReceiptsCount: Flow<Int> = receiptDao.getTotalReceiptsCount()
    val totalSales: Flow<Double> = receiptDao.getTotalSales()

    fun getTodayReceiptsCount(): Flow<Int> {
        val startOfDay = getStartOfDayTimestamp()
        return receiptDao.getTodayReceiptsCount(startOfDay)
    }

    fun getTodayTotalSales(): Flow<Double> {
        val startOfDay = getStartOfDayTimestamp()
        return receiptDao.getTodayTotalSales(startOfDay)
    }

    fun getRecentReceipts(limit: Int = 5): Flow<List<ReceiptEntity>> {
        return receiptDao.getRecentReceipts(limit)
    }

    fun searchReceipts(query: String): Flow<List<ReceiptEntity>> {
        return receiptDao.searchReceipts(query)
    }

    fun getReceiptsBetween(start: Long, end: Long): Flow<List<ReceiptEntity>> {
        return receiptDao.getReceiptsBetween(start, end)
    }

    suspend fun getReceiptById(id: Long): ReceiptEntity? {
        return receiptDao.getReceiptById(id)
    }

    suspend fun getReceiptByNumber(number: String): ReceiptEntity? {
        return receiptDao.getReceiptByNumber(number)
    }

    suspend fun getMaxSequenceNumber(): Long {
        return receiptDao.getMaxSequenceNumber() ?: 0L
    }

    suspend fun insertReceipt(receipt: ReceiptEntity): Long {
        return receiptDao.insertReceipt(receipt)
    }

    suspend fun updateReceipt(receipt: ReceiptEntity) {
        receiptDao.updateReceipt(receipt)
    }

    suspend fun deleteReceiptById(id: Long) {
        receiptDao.deleteReceiptById(id)
    }

    private fun getStartOfDayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
