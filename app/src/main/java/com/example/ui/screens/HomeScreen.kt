package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.local.entity.ReceiptEntity
import com.example.ui.components.QuickNavTile
import com.example.ui.components.StatCard
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.viewmodel.ReceiptViewModel
import com.example.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val todayReceiptsCount by viewModel.todayReceiptsCount.collectAsStateWithLifecycle()
    val todayTotalSales by viewModel.todayTotalSales.collectAsStateWithLifecycle()
    val totalReceiptsCount by viewModel.totalReceiptsCount.collectAsStateWithLifecycle()
    val recentReceipts by viewModel.recentReceipts.collectAsStateWithLifecycle()

    val currency = shopProfile?.currency ?: "PKR"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate50
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // App Header with Shop Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!shopProfile?.logoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = shopProfile?.logoUri,
                                contentDescription = "Shop Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Slate200, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(NavyPrimary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }

                        Column {
                            Text(
                                text = "Receipt Go",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = shopProfile?.shopName?.ifBlank { "Create. Print. Share." } ?: "Create. Print. Share.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TealAccent
                            )
                        }
                    }

                    // Edit shop profile quick button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.navigateTo(Screen.SHOP_PROFILE) }
                            .testTag("home_shop_profile_button"),
                        color = Color.White,
                        shape = CircleShape,
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = "Shop Profile",
                                tint = Slate700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // PRIMARY HERO BUTTON: + Create Receipt
            item {
                Button(
                    onClick = { viewModel.startNewReceipt() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("create_receipt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "+ Create Receipt",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // BUSINESS STATISTICS ROW
            item {
                Column {
                    Text(
                        text = "Dashboard Overview",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Today's Sales",
                            value = "$currency ${formatCompact(todayTotalSales)}",
                            icon = Icons.Default.AttachMoney,
                            iconTint = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Today's Receipts",
                            value = "$todayReceiptsCount",
                            icon = Icons.Default.Today,
                            iconTint = BlueAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Total All-time",
                            value = "$totalReceiptsCount",
                            icon = Icons.Default.Receipt,
                            iconTint = TealAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // QUICK NAVIGATION TILES
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavTile(
                            title = "History",
                            subtitle = "View all receipts",
                            icon = Icons.Default.History,
                            iconColor = BlueAccent,
                            onClick = { viewModel.navigateTo(Screen.RECEIPT_HISTORY) },
                            modifier = Modifier.weight(1f),
                            testTag = "nav_history_tile"
                        )
                        QuickNavTile(
                            title = "Printer",
                            subtitle = "Bluetooth & POS",
                            icon = Icons.Default.Print,
                            iconColor = TealAccent,
                            onClick = { viewModel.navigateTo(Screen.PRINTER) },
                            modifier = Modifier.weight(1f),
                            testTag = "nav_printer_tile"
                        )
                    }
                }
            }

            // RECENT RECEIPTS SECTION
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Receipts",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                    if (recentReceipts.isNotEmpty()) {
                        Text(
                            text = "View All",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealAccent,
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(Screen.RECEIPT_HISTORY) }
                                .padding(4.dp)
                                .testTag("view_all_receipts_link")
                        )
                    }
                }
            }

            if (recentReceipts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Slate500.copy(alpha = 0.6f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No receipts generated yet",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate700
                            )
                            Text(
                                text = "Tap '+ Create Receipt' to generate your first receipt in seconds",
                                fontSize = 12.sp,
                                color = Slate500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            } else {
                items(recentReceipts, key = { it.id }) { receipt ->
                    RecentReceiptCard(
                        receipt = receipt,
                        onView = { viewModel.selectReceiptForView(receipt) },
                        onShare = { viewModel.shareReceiptFile(context, receipt, asImage = false) },
                        onPrint = { viewModel.printThermalReceipt(receipt) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun RecentReceiptCard(
    receipt: ReceiptEntity,
    onView: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView)
            .testTag("receipt_card_${receipt.receiptNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = receipt.receiptNumber,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TealAccent.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = receipt.paymentMethod,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (receipt.customerName.isNotBlank()) receipt.customerName else "Walk-in Customer",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )
                Text(
                    text = "${receipt.formattedDateTime} • ${receipt.getItems().size} items",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${receipt.currency} ${formatCompact(receipt.totalAmount)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Slate600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onPrint,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = Slate600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatCompact(number: Double): String {
    return if (number % 1.0 == 0.0) {
        String.format("%,d", number.toLong())
    } else {
        String.format("%,.2f", number)
    }
}
