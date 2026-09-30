package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.ShopProfile
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

object ReceiptExporter {

    private const val FILE_PROVIDER_AUTHORITY = "com.aistudio.receiptgo.app.fileprovider"

    fun generateReceiptPdf(
        context: Context,
        receipt: ReceiptEntity,
        shopProfile: ShopProfile?,
        paperWidthType: String = "80mm"
    ): File? {
        try {
            val pageWidth = if (paperWidthType == "58mm") 280 else if (paperWidthType == "80mm") 380 else 595
            val items = receipt.getItems()
            
            // Dynamic page height based on items
            val baseHeight = 450
            val itemHeight = items.size * 28
            val pageHeight = baseHeight + itemHeight

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawReceiptOnCanvas(canvas, pageWidth.toFloat(), receipt, shopProfile, paperWidthType)

            pdfDocument.finishPage(page)

            val receiptsDir = File(context.cacheDir, "receipts")
            if (!receiptsDir.exists()) receiptsDir.mkdirs()

            val pdfFile = File(receiptsDir, "${receipt.receiptNumber}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generateReceiptImage(
        context: Context,
        receipt: ReceiptEntity,
        shopProfile: ShopProfile?,
        paperWidthType: String = "80mm"
    ): File? {
        try {
            val scale = 2f
            val pageWidth = (if (paperWidthType == "58mm") 320 else 440) * scale.toInt()
            val items = receipt.getItems()
            val baseHeight = 500 * scale.toInt()
            val itemHeight = (items.size * 32) * scale.toInt()
            val pageHeight = baseHeight + itemHeight

            val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // White background
            canvas.drawColor(Color.WHITE)

            canvas.save()
            canvas.scale(scale, scale)
            drawReceiptOnCanvas(canvas, (pageWidth / scale), receipt, shopProfile, paperWidthType)
            canvas.restore()

            val receiptsDir = File(context.cacheDir, "receipts")
            if (!receiptsDir.exists()) receiptsDir.mkdirs()

            val imageFile = File(receiptsDir, "${receipt.receiptNumber}.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun drawReceiptOnCanvas(
        canvas: Canvas,
        pageWidth: Float,
        receipt: ReceiptEntity,
        shopProfile: ShopProfile?,
        paperWidthType: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textDark = Color.rgb(15, 23, 42) // Slate 900
        val textMuted = Color.rgb(100, 116, 139) // Slate 500
        val lineColor = Color.rgb(226, 232, 240) // Slate 200

        var currentY = 36f
        val margin = 20f
        val contentWidth = pageWidth - (margin * 2)

        // Shop Name Header
        paint.color = textDark
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val shopName = receipt.shopNameSnapshot.ifBlank { shopProfile?.shopName ?: "Receipt Go Store" }
        var textWidth = paint.measureText(shopName)
        canvas.drawText(shopName, (pageWidth - textWidth) / 2f, currentY, paint)
        currentY += 20f

        // Tagline / Phone / Address
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = textMuted

        val phone = receipt.shopPhoneSnapshot.ifBlank { shopProfile?.phone ?: "" }
        if (phone.isNotBlank()) {
            val phoneText = "Tel: $phone"
            textWidth = paint.measureText(phoneText)
            canvas.drawText(phoneText, (pageWidth - textWidth) / 2f, currentY, paint)
            currentY += 15f
        }

        val address = receipt.shopAddressSnapshot.ifBlank { shopProfile?.address ?: "" }
        if (address.isNotBlank()) {
            textWidth = paint.measureText(address)
            canvas.drawText(address, (pageWidth - textWidth) / 2f, currentY, paint)
            currentY += 15f
        }

        currentY += 8f
        // Divider
        paint.color = lineColor
        paint.strokeWidth = 1.5f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 18f

        // Receipt Meta (No, Date, Customer, Payment)
        paint.color = textMuted
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Receipt No:", margin, currentY, paint)
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        var rightText = receipt.receiptNumber
        textWidth = paint.measureText(rightText)
        canvas.drawText(rightText, pageWidth - margin - textWidth, currentY, paint)
        currentY += 16f

        paint.color = textMuted
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Date & Time:", margin, currentY, paint)
        paint.color = textDark
        rightText = receipt.formattedDateTime
        textWidth = paint.measureText(rightText)
        canvas.drawText(rightText, pageWidth - margin - textWidth, currentY, paint)
        currentY += 16f

        if (receipt.customerName.isNotBlank()) {
            paint.color = textMuted
            canvas.drawText("Customer:", margin, currentY, paint)
            paint.color = textDark
            rightText = receipt.customerName
            textWidth = paint.measureText(rightText)
            canvas.drawText(rightText, pageWidth - margin - textWidth, currentY, paint)
            currentY += 16f
        }

        paint.color = textMuted
        canvas.drawText("Payment:", margin, currentY, paint)
        paint.color = Color.rgb(13, 148, 136) // Teal
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        rightText = receipt.paymentMethod
        textWidth = paint.measureText(rightText)
        canvas.drawText(rightText, pageWidth - margin - textWidth, currentY, paint)
        currentY += 16f

        // Divider
        paint.color = lineColor
        paint.strokeWidth = 1f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 18f

        // Table Header
        paint.color = textDark
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ITEM", margin, currentY, paint)
        
        val qtyHeader = "QTY"
        val totalHeader = "TOTAL"
        val totalHeadWidth = paint.measureText(totalHeader)
        val qtyHeadWidth = paint.measureText(qtyHeader)

        canvas.drawText(qtyHeader, pageWidth - margin - totalHeadWidth - 50f, currentY, paint)
        canvas.drawText(totalHeader, pageWidth - margin - totalHeadWidth, currentY, paint)
        currentY += 10f

        paint.color = lineColor
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 18f

        // Items
        val items = receipt.getItems()
        val currency = receipt.currency
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f

        for (item in items) {
            paint.color = textDark
            val itemName = if (item.name.length > 20) item.name.take(19) + "…" else item.name
            canvas.drawText(itemName, margin, currentY, paint)

            val qtyStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()} ${item.unit}" else "${item.quantity} ${item.unit}"
            paint.color = textMuted
            canvas.drawText(qtyStr, pageWidth - margin - totalHeadWidth - 50f, currentY, paint)

            paint.color = textDark
            val itemTotalStr = formatCurrency(item.totalPrice)
            val itemTotalW = paint.measureText(itemTotalStr)
            canvas.drawText(itemTotalStr, pageWidth - margin - itemTotalW, currentY, paint)

            currentY += 24f
        }

        // Divider
        paint.color = lineColor
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 18f

        // Subtotal
        paint.color = textMuted
        paint.textSize = 11f
        canvas.drawText("Subtotal:", margin, currentY, paint)
        paint.color = textDark
        var valStr = "$currency ${formatCurrency(receipt.subtotal)}"
        textWidth = paint.measureText(valStr)
        canvas.drawText(valStr, pageWidth - margin - textWidth, currentY, paint)
        currentY += 18f

        // Discount
        if (receipt.discountCalculated > 0) {
            paint.color = Color.rgb(225, 29, 72) // Rose / red
            val discLabel = if (receipt.discountType == "PERCENT") "Discount (${receipt.discountValue.toInt()}%):" else "Discount:"
            canvas.drawText(discLabel, margin, currentY, paint)
            valStr = "- $currency ${formatCurrency(receipt.discountCalculated)}"
            textWidth = paint.measureText(valStr)
            canvas.drawText(valStr, pageWidth - margin - textWidth, currentY, paint)
            currentY += 18f
        }

        // Tax
        if (receipt.taxCalculated > 0) {
            paint.color = textMuted
            val taxLabel = if (receipt.taxType == "PERCENT") "Tax / GST (${receipt.taxValue.toInt()}%):" else "Tax:"
            canvas.drawText(taxLabel, margin, currentY, paint)
            paint.color = textDark
            valStr = "+ $currency ${formatCurrency(receipt.taxCalculated)}"
            textWidth = paint.measureText(valStr)
            canvas.drawText(valStr, pageWidth - margin - textWidth, currentY, paint)
            currentY += 18f
        }

        // Strong Divider
        paint.color = textDark
        paint.strokeWidth = 1.5f
        canvas.drawLine(margin, currentY, pageWidth - margin, currentY, paint)
        currentY += 22f

        // TOTAL
        paint.color = textDark
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL", margin, currentY, paint)

        val totalStr = "$currency ${formatCurrency(receipt.totalAmount)}"
        textWidth = paint.measureText(totalStr)
        canvas.drawText(totalStr, pageWidth - margin - textWidth, currentY, paint)
        currentY += 28f

        // Bottom Footer
        paint.color = textMuted
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val note = receipt.notes.ifBlank { "Thank You! Please Visit Again." }
        textWidth = paint.measureText(note)
        canvas.drawText(note, (pageWidth - textWidth) / 2f, currentY, paint)
        currentY += 16f

        val appTag = "Generated with Receipt Go"
        textWidth = paint.measureText(appTag)
        canvas.drawText(appTag, (pageWidth - textWidth) / 2f, currentY, paint)
    }

    private fun formatCurrency(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format("%,d", amount.toLong())
        } else {
            String.format("%,.2f", amount)
        }
    }

    fun shareReceipt(context: Context, file: File, mimeType: String, title: String = "Share Receipt") {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Here is your receipt from ${file.nameWithoutExtension}. Thank you!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open share dialog", Toast.LENGTH_SHORT).show()
        }
    }

    fun printViaSystemManager(context: Context, pdfFile: File) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val jobName = "Receipt_${pdfFile.nameWithoutExtension}"

            printManager?.print(
                jobName,
                object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes?,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback?,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onLayoutCancelled()
                            return
                        }
                        val info = PrintDocumentInfo.Builder(pdfFile.name)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback?.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor?,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback?
                    ) {
                        try {
                            val input = FileInputStream(pdfFile)
                            val output = FileOutputStream(destination?.fileDescriptor)
                            val buf = ByteArray(1024)
                            var bytesRead: Int
                            while (input.read(buf).also { bytesRead = it } > 0) {
                                output.write(buf, 0, bytesRead)
                            }
                            input.close()
                            output.close()
                            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback?.onWriteFailed(e.message)
                        }
                    }
                },
                PrintAttributes.Builder().build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "System print failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
