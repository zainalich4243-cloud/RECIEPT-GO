package com.example.util

import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.ShopProfile
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object ThermalEscPosHelper {

    private val CHARSET: Charset = Charset.forName("CP437")

    // ESC/POS Command constants
    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D

    val CMD_INIT = byteArrayOf(ESC, 0x40) // ESC @
    val CMD_ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00) // ESC a 0
    val CMD_ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01) // ESC a 1
    val CMD_ALIGN_RIGHT = byteArrayOf(ESC, 0x61, 0x02) // ESC a 2

    val CMD_BOLD_ON = byteArrayOf(ESC, 0x45, 0x01) // ESC E 1
    val CMD_BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00) // ESC E 0

    val CMD_DOUBLE_SIZE = byteArrayOf(GS, 0x21, 0x11) // GS ! 17 (2x width & height)
    val CMD_DOUBLE_HEIGHT = byteArrayOf(GS, 0x21, 0x01) // GS ! 1
    val CMD_DOUBLE_WIDTH = byteArrayOf(GS, 0x21, 0x10) // GS ! 16
    val CMD_NORMAL_SIZE = byteArrayOf(GS, 0x21, 0x00) // GS ! 0

    val CMD_FEED_CUT = byteArrayOf(0x0A, 0x0A, 0x0A, 0x0A, GS, 0x56, 0x42, 0x00) // Cut paper

    fun generateTestSlip(paperSize: String = "58mm"): ByteArray {
        val maxCols = if (paperSize == "80mm") 48 else 32
        val stream = ByteArrayOutputStream()

        stream.write(CMD_INIT)
        stream.write(CMD_ALIGN_CENTER)
        stream.write(CMD_BOLD_ON)
        stream.write(CMD_DOUBLE_SIZE)
        stream.write("RECEIPT GO\n".toByteArray(CHARSET))
        stream.write(CMD_NORMAL_SIZE)
        stream.write(CMD_BOLD_OFF)
        stream.write("Thermal Printer Test\n".toByteArray(CHARSET))
        stream.write(CMD_ALIGN_CENTER)
        stream.write("${"-".repeat(maxCols)}\n".toByteArray(CHARSET))

        stream.write(CMD_ALIGN_LEFT)
        stream.write(formatTwoColumns("Status:", "Connected OK", maxCols).toByteArray(CHARSET))
        stream.write(formatTwoColumns("Paper Width:", paperSize, maxCols).toByteArray(CHARSET))
        stream.write(formatTwoColumns("Columns:", "$maxCols chars", maxCols).toByteArray(CHARSET))
        stream.write(formatTwoColumns("Mode:", "ESC/POS", maxCols).toByteArray(CHARSET))

        stream.write(CMD_ALIGN_CENTER)
        stream.write("${"-".repeat(maxCols)}\n".toByteArray(CHARSET))
        stream.write(CMD_BOLD_ON)
        stream.write("Test Print Successful!\n".toByteArray(CHARSET))
        stream.write(CMD_BOLD_OFF)
        stream.write("Ready to print customer receipts\n".toByteArray(CHARSET))
        stream.write(CMD_FEED_CUT)

        return stream.toByteArray()
    }

    fun generateReceiptBytes(
        receipt: ReceiptEntity,
        shopProfile: ShopProfile?,
        paperSize: String = "58mm",
        autoCut: Boolean = true
    ): ByteArray {
        val maxCols = if (paperSize == "80mm") 48 else 32
        val stream = ByteArrayOutputStream()
        val items = receipt.getItems()
        val currency = receipt.currency

        stream.write(CMD_INIT)

        // Header: Shop Name & Tagline
        stream.write(CMD_ALIGN_CENTER)
        stream.write(CMD_BOLD_ON)
        stream.write(CMD_DOUBLE_HEIGHT)
        val shopName = receipt.shopNameSnapshot.ifBlank { shopProfile?.shopName ?: "Receipt Go Shop" }
        stream.write("$shopName\n".toByteArray(CHARSET))
        stream.write(CMD_NORMAL_SIZE)
        stream.write(CMD_BOLD_OFF)

        val phone = receipt.shopPhoneSnapshot.ifBlank { shopProfile?.phone ?: "" }
        if (phone.isNotBlank()) {
            stream.write("Phone: $phone\n".toByteArray(CHARSET))
        }

        val address = receipt.shopAddressSnapshot.ifBlank { shopProfile?.address ?: "" }
        if (address.isNotBlank()) {
            stream.write("$address\n".toByteArray(CHARSET))
        }

        stream.write("${"=".repeat(maxCols)}\n".toByteArray(CHARSET))

        // Receipt Metadata
        stream.write(CMD_ALIGN_LEFT)
        stream.write(formatTwoColumns("Receipt No:", receipt.receiptNumber, maxCols).toByteArray(CHARSET))
        stream.write(formatTwoColumns("Date:", receipt.formattedDateTime, maxCols).toByteArray(CHARSET))
        if (receipt.customerName.isNotBlank()) {
            stream.write(formatTwoColumns("Customer:", receipt.customerName, maxCols).toByteArray(CHARSET))
        }
        stream.write(formatTwoColumns("Payment:", receipt.paymentMethod, maxCols).toByteArray(CHARSET))

        stream.write("${"-".repeat(maxCols)}\n".toByteArray(CHARSET))

        // Items Table Header
        stream.write(CMD_BOLD_ON)
        if (maxCols >= 48) {
            // 80mm: Item (22) | Qty (8) | Price (8) | Total (10)
            val header = formatFourColumns("Item", "Qty", "Price", "Total", maxCols)
            stream.write("$header\n".toByteArray(CHARSET))
        } else {
            // 58mm: Item (16) | Qty (6) | Total (10)
            val header = formatThreeColumns("Item", "Qty", "Total", maxCols)
            stream.write("$header\n".toByteArray(CHARSET))
        }
        stream.write(CMD_BOLD_OFF)
        stream.write("${"-".repeat(maxCols)}\n".toByteArray(CHARSET))

        // Line Items
        for (item in items) {
            val qtyStr = if (item.quantity % 1.0 == 0.0) {
                "${item.quantity.toInt()} ${item.unit}"
            } else {
                "${item.quantity} ${item.unit}"
            }
            val priceStr = formatAmount(item.unitPrice)
            val totalStr = formatAmount(item.totalPrice)

            if (maxCols >= 48) {
                val line = formatFourColumns(item.name, qtyStr, priceStr, totalStr, maxCols)
                stream.write("$line\n".toByteArray(CHARSET))
            } else {
                val line = formatThreeColumns(item.name, qtyStr, totalStr, maxCols)
                stream.write("$line\n".toByteArray(CHARSET))
            }
        }

        stream.write("${"-".repeat(maxCols)}\n".toByteArray(CHARSET))

        // Financials Summary
        stream.write(CMD_ALIGN_LEFT)
        stream.write(formatTwoColumns("Subtotal:", "$currency ${formatAmount(receipt.subtotal)}", maxCols).toByteArray(CHARSET))

        if (receipt.discountCalculated > 0) {
            val discLabel = if (receipt.discountType == "PERCENT") "Discount (${receipt.discountValue.toInt()}%):" else "Discount:"
            stream.write(formatTwoColumns(discLabel, "- $currency ${formatAmount(receipt.discountCalculated)}", maxCols).toByteArray(CHARSET))
        }

        if (receipt.taxCalculated > 0) {
            val taxLabel = if (receipt.taxType == "PERCENT") "Tax/GST (${receipt.taxValue.toInt()}%):" else "Tax:"
            stream.write(formatTwoColumns(taxLabel, "+ $currency ${formatAmount(receipt.taxCalculated)}", maxCols).toByteArray(CHARSET))
        }

        stream.write("${"=".repeat(maxCols)}\n".toByteArray(CHARSET))

        // TOTAL in Bold Double Height
        stream.write(CMD_BOLD_ON)
        stream.write(CMD_DOUBLE_HEIGHT)
        stream.write(formatTwoColumns("TOTAL:", "$currency ${formatAmount(receipt.totalAmount)}", maxCols).toByteArray(CHARSET))
        stream.write(CMD_NORMAL_SIZE)
        stream.write(CMD_BOLD_OFF)

        stream.write("${"=".repeat(maxCols)}\n".toByteArray(CHARSET))

        // Footer note
        stream.write(CMD_ALIGN_CENTER)
        val note = receipt.notes.ifBlank { "Thank You! Please Visit Again." }
        stream.write("$note\n".toByteArray(CHARSET))
        stream.write("Powered by Receipt Go\n".toByteArray(CHARSET))

        if (autoCut) {
            stream.write(CMD_FEED_CUT)
        } else {
            stream.write("\n\n\n\n".toByteArray(CHARSET))
        }

        return stream.toByteArray()
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    private fun formatTwoColumns(left: String, right: String, totalWidth: Int): String {
        val spaces = totalWidth - left.length - right.length
        return if (spaces >= 0) {
            left + " ".repeat(spaces) + right + "\n"
        } else {
            // Truncate left if needed
            val maxLeft = totalWidth - right.length - 1
            if (maxLeft > 0) {
                left.take(maxLeft) + " " + right + "\n"
            } else {
                "$left\n$right\n"
            }
        }
    }

    private fun formatThreeColumns(col1: String, col2: String, col3: String, totalWidth: Int): String {
        val c2Width = 8
        val c3Width = 10
        val c1Width = totalWidth - c2Width - c3Width

        val safeC1 = if (col1.length > c1Width) col1.take(c1Width - 1) + " " else col1.padEnd(c1Width)
        val safeC2 = col2.padStart(c2Width)
        val safeC3 = col3.padStart(c3Width)
        return safeC1 + safeC2 + safeC3
    }

    private fun formatFourColumns(col1: String, col2: String, col3: String, col4: String, totalWidth: Int): String {
        val c2Width = 8
        val c3Width = 10
        val c4Width = 11
        val c1Width = totalWidth - c2Width - c3Width - c4Width

        val safeC1 = if (col1.length > c1Width) col1.take(c1Width - 1) + " " else col1.padEnd(c1Width)
        val safeC2 = col2.padStart(c2Width)
        val safeC3 = col3.padStart(c3Width)
        val safeC4 = col4.padStart(c4Width)
        return safeC1 + safeC2 + safeC3 + safeC4
    }
}
