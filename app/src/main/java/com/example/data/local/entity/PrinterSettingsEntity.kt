package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "printer_settings")
data class PrinterSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val connectedPrinterName: String? = null,
    val connectedPrinterAddress: String? = null,
    val paperSize: String = "58mm", // "58mm" or "80mm"
    val autoCut: Boolean = true,
    val printCopies: Int = 1,
    val includeLogo: Boolean = true,
    val includeBarcode: Boolean = true,
    val customHeaderNote: String = "",
    val customFooterNote: String = "Thank You!"
)
