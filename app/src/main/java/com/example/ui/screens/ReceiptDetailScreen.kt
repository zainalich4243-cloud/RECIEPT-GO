package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.ReceiptGoTopAppBar
import com.example.ui.components.ReceiptPaperView
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.util.AdManager
import com.example.viewmodel.ReceiptViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDetailScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val receipt by viewModel.selectedReceipt.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val printerSettings by viewModel.printerSettings.collectAsStateWithLifecycle()

    var paperWidthType by remember { mutableStateOf("80mm") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    if (receipt == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Receipt not found")
        }
        return
    }

    val currentReceipt = receipt!!

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate50,
        topBar = {
            ReceiptGoTopAppBar(
                title = currentReceipt.receiptNumber,
                subtitle = currentReceipt.formattedDateTime,
                onBackClick = { viewModel.navigateBack() },
                actions = {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_receipt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Receipt",
                            tint = Color(0xFFE11D48)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Action Bar: Print, PDF, Image, Share
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Print Receipt (Primary)
                        Button(
                            onClick = { viewModel.printThermalReceipt(currentReceipt) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("print_receipt_action_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print Receipt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Share Receipt (WhatsApp, etc.)
                        Button(
                            onClick = {
                                AdManager.showRewardedAd(
                                    activity = null,
                                    rewardActionName = "Share Receipt",
                                    onUserEarnedReward = {
                                        viewModel.shareReceiptFile(context, currentReceipt, asImage = false)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("share_receipt_action_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary Export Options: Download PDF / PNG
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                AdManager.showRewardedAd(
                                    activity = null,
                                    rewardActionName = "Export PDF",
                                    onUserEarnedReward = {
                                        viewModel.exportReceiptAsPdf(context, currentReceipt) {}
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                AdManager.showRewardedAd(
                                    activity = null,
                                    rewardActionName = "Export Image",
                                    onUserEarnedReward = {
                                        viewModel.exportReceiptAsImage(context, currentReceipt) {}
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("export_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export Image", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Paper Size Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receipt Layout Format:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )
                Row {
                    FilterChip(
                        selected = paperWidthType == "58mm",
                        onClick = { paperWidthType = "58mm" },
                        label = { Text("58mm Thermal", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = paperWidthType == "80mm",
                        onClick = { paperWidthType = "80mm" },
                        label = { Text("80mm / Standard", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NavyPrimary,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // The Rendered Receipt View
            ReceiptPaperView(
                receipt = currentReceipt,
                shopProfile = shopProfile,
                paperWidthType = paperWidthType,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Receipt?") },
            text = { Text("Are you sure you want to delete ${currentReceipt.receiptNumber}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteReceipt(currentReceipt.id) {
                            viewModel.navigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
