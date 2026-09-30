package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ReceiptEntity
import com.example.ui.components.ReceiptGoTopAppBar
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.viewmodel.DateFilter
import com.example.viewmodel.ReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptHistoryScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val receipts by viewModel.allReceipts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()

    var receiptToDelete by remember { mutableStateOf<ReceiptEntity?>(null) }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate50,
        topBar = {
            ReceiptGoTopAppBar(
                title = "Receipt History",
                subtitle = "${receipts.size} receipts found",
                onBackClick = { viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar: Search by Receipt No, Customer Name, or Product Name
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by receipt #, customer, product...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Slate500)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("receipt_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Date Filters Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(DateFilter.values()) { filter ->
                    val label = when (filter) {
                        DateFilter.ALL -> "All"
                        DateFilter.TODAY -> "Today"
                        DateFilter.YESTERDAY -> "Yesterday"
                        DateFilter.THIS_WEEK -> "This Week"
                        DateFilter.THIS_MONTH -> "This Month"
                    }
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.setDateFilter(filter) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("date_filter_$label")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Receipts List
            if (receipts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Slate500.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching receipts" else "No receipts in history",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate700
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Try a different search term" else "Created receipts will be stored here",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(receipts, key = { it.id }) { receipt ->
                        HistoryReceiptItemCard(
                            receipt = receipt,
                            onView = { viewModel.selectReceiptForView(receipt) },
                            onShare = { viewModel.shareReceiptFile(context, receipt, asImage = false) },
                            onPrint = { viewModel.printThermalReceipt(receipt) },
                            onDelete = { receiptToDelete = receipt }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (receiptToDelete != null) {
        val item = receiptToDelete!!
        AlertDialog(
            onDismissRequest = { receiptToDelete = null },
            title = { Text("Delete Receipt?") },
            text = { Text("Are you sure you want to delete ${item.receiptNumber}? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReceipt(item.id)
                        receiptToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HistoryReceiptItemCard(
    receipt: ReceiptEntity,
    onView: () -> Unit,
    onShare: () -> Unit,
    onPrint: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onView)
            .testTag("history_card_${receipt.receiptNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = receipt.receiptNumber,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TealAccent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = receipt.paymentMethod,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${receipt.currency} ${formatNumber(receipt.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (receipt.customerName.isNotBlank()) receipt.customerName else "Walk-in Customer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700
                    )
                    Text(
                        text = "${receipt.formattedDateTime} • ${receipt.getItems().size} items",
                        fontSize = 11.5.sp,
                        color = Slate500
                    )
                }

                // Quick Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onView,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onPrint,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print",
                            tint = Slate700,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
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
