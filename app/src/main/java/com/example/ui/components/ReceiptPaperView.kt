package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.ShopProfile
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.ReceiptPaperWhite
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent

@Composable
fun ReceiptPaperView(
    receipt: ReceiptEntity,
    shopProfile: ShopProfile?,
    modifier: Modifier = Modifier,
    paperWidthType: String = "80mm"
) {
    val items = receipt.getItems()
    val shopName = receipt.shopNameSnapshot.ifBlank { shopProfile?.shopName ?: "Receipt Go Store" }
    val phone = receipt.shopPhoneSnapshot.ifBlank { shopProfile?.phone ?: "" }
    val address = receipt.shopAddressSnapshot.ifBlank { shopProfile?.address ?: "" }
    val logoUri = receipt.shopLogoUriSnapshot ?: shopProfile?.logoUri
    val currency = receipt.currency

    val contentPadding = if (paperWidthType == "58mm") 16.dp else 24.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(12.dp))
            .testTag("receipt_paper_view"),
        shape = RoundedCornerShape(12.dp),
        color = ReceiptPaperWhite,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shop Logo (if available) or Shop Icon
            if (!logoUri.isNullOrBlank()) {
                AsyncImage(
                    model = logoUri,
                    contentDescription = "Shop Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .border(1.dp, Slate200, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NavyPrimary.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Shop Name
            Text(
                text = shopName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                textAlign = TextAlign.Center
            )

            if (phone.isNotBlank()) {
                Text(
                    text = "Tel: $phone",
                    fontSize = 12.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )
            }

            if (address.isNotBlank()) {
                Text(
                    text = address,
                    fontSize = 12.sp,
                    color = Slate500,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Metadata: Receipt Number, Date, Customer, Payment
            ReceiptMetaRow(label = "Receipt No.", value = receipt.receiptNumber, isBold = true)
            ReceiptMetaRow(label = "Date & Time", value = receipt.formattedDateTime)
            if (receipt.customerName.isNotBlank()) {
                ReceiptMetaRow(label = "Customer", value = receipt.customerName)
            }
            ReceiptMetaRow(
                label = "Payment",
                value = receipt.paymentMethod,
                valueColor = TealAccent,
                isBold = true
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ITEM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    text = "QTY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = "TOTAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate200, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Items List
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = item.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )
                        Text(
                            text = "@ $currency ${formatNumber(item.unitPrice)}/${item.unit}",
                            fontSize = 10.5.sp,
                            color = Slate500
                        )
                    }

                    val qtyStr = if (item.quantity % 1.0 == 0.0) {
                        "${item.quantity.toInt()} ${item.unit}"
                    } else {
                        "${item.quantity} ${item.unit}"
                    }

                    Text(
                        text = qtyStr,
                        fontSize = 12.5.sp,
                        color = Slate700,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(0.9f)
                    )

                    Text(
                        text = "$currency ${formatNumber(item.totalPrice)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Subtotal
            ReceiptSummaryRow(
                label = "Subtotal",
                value = "$currency ${formatNumber(receipt.subtotal)}"
            )

            // Discount
            if (receipt.discountCalculated > 0) {
                val discLabel = if (receipt.discountType == "PERCENT") "Discount (${receipt.discountValue.toInt()}%)" else "Discount"
                ReceiptSummaryRow(
                    label = discLabel,
                    value = "- $currency ${formatNumber(receipt.discountCalculated)}",
                    valueColor = Color(0xFFE11D48)
                )
            }

            // Tax
            if (receipt.taxCalculated > 0) {
                val taxLabel = if (receipt.taxType == "PERCENT") "Tax / GST (${receipt.taxValue.toInt()}%)" else "Tax"
                ReceiptSummaryRow(
                    label = taxLabel,
                    value = "+ $currency ${formatNumber(receipt.taxCalculated)}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate900, thickness = 1.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // TOTAL in bold large font
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
                Text(
                    text = "$currency ${formatNumber(receipt.totalAmount)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Footer Note
            val footer = receipt.notes.ifBlank { "Thank You! Please Visit Again." }
            Text(
                text = footer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Slate700,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Receipt Go • Fast & Reliable Receipts",
                fontSize = 10.sp,
                color = Slate400,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Decorative Barcode Representation
            ReceiptBarcode()
        }
    }
}

@Composable
private fun ReceiptMetaRow(
    label: String,
    value: String,
    valueColor: Color = Slate900,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Slate500)
        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReceiptSummaryRow(
    label: String,
    value: String,
    valueColor: Color = Slate900
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Slate500)
        Text(text = value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ReceiptBarcode() {
    Row(
        modifier = Modifier
            .height(24.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bars = listOf(3, 1, 2, 4, 1, 3, 2, 1, 4, 2, 1, 3, 2, 4, 1, 3, 1, 2, 3, 1, 4, 2)
        bars.forEachIndexed { index, width ->
            val color = if (index % 2 == 0) Slate900 else Color.Transparent
            Box(
                modifier = Modifier
                    .width(width.dp)
                    .height(20.dp)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}

private fun formatNumber(number: Double): String {
    return if (number % 1.0 == 0.0) {
        String.format("%,d", number.toLong())
    } else {
        String.format("%,.2f", number)
    }
}
