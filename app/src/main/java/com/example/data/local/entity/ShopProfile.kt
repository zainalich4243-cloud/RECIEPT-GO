package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_profile")
data class ShopProfile(
    @PrimaryKey val id: Int = 1,
    val shopName: String = "",
    val logoUri: String? = null,
    val phone: String = "",
    val address: String = "",
    val tagline: String = "Create. Print. Share.",
    val currency: String = "PKR",
    val currencySymbol: String = "₨",
    val defaultTaxPercent: Double = 0.0,
    val defaultFooter: String = "Thank you for your business! Please visit again.",
    val receiptPrefix: String = "RC-",
    val nextReceiptSequence: Long = 1L,
    val isSetupCompleted: Boolean = false
)
