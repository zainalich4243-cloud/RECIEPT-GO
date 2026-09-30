package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ReceiptItem

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String, // e.g. RC-000001
    val sequenceNumber: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val formattedDateTime: String,
    val customerName: String = "",
    val customerPhone: String = "",
    val paymentMethod: String = "Cash", // Cash, Card, EasyPaisa, JazzCash, Online, Credit
    val subtotal: Double = 0.0,
    val discountType: String = "AMOUNT", // AMOUNT or PERCENT
    val discountValue: Double = 0.0,
    val discountCalculated: Double = 0.0,
    val taxType: String = "PERCENT", // AMOUNT or PERCENT
    val taxValue: Double = 0.0,
    val taxCalculated: Double = 0.0,
    val totalAmount: Double = 0.0,
    val currency: String = "PKR",
    val notes: String = "Thank You! Please Visit Again.",
    val itemsJson: String = "[]",
    val shopNameSnapshot: String = "",
    val shopPhoneSnapshot: String = "",
    val shopAddressSnapshot: String = "",
    val shopLogoUriSnapshot: String? = null,
    val status: String = "COMPLETED" // COMPLETED, VOID
) {
    fun getItems(): List<ReceiptItem> {
        return ReceiptItem.listFromJsonString(itemsJson)
    }
}
