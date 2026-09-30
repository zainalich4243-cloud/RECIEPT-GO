package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.viewmodel.ReceiptViewModel

@Composable
fun FirstTimeSetupScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    var shopName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<String?>(null) }
    var shopNameError by remember { mutableStateOf(false) }

    // Photo picker for logo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri.toString()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(NavyPrimary, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Receipt Go",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Text(
                text = "Create. Print. Share.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TealAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Shop Setup",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Enter your business details once. These will automatically appear on all your printed and digital receipts.",
                        fontSize = 12.5.sp,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Logo Uploader / Picker
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.5.dp, if (logoUri != null) TealAccent else Slate200, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("shop_logo_picker"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri != null) {
                            AsyncImage(
                                model = logoUri,
                                contentDescription = "Shop Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Add Logo",
                                    tint = Slate500,
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "Logo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    if (logoUri != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Logo added",
                                fontSize = 11.sp,
                                color = TealAccent,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { logoUri = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Logo",
                                    tint = Slate500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "(Optional logo)",
                            fontSize = 11.sp,
                            color = Slate500,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Shop Name (Required)
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = {
                            shopName = it
                            if (it.isNotBlank()) shopNameError = false
                        },
                        label = { Text("Shop Name *") },
                        placeholder = { Text("e.g. Al-Madina Super Store") },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = NavyPrimary)
                        },
                        isError = shopNameError,
                        supportingText = {
                            if (shopNameError) {
                                Text("Shop Name is required to continue", color = Color.Red)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Phone Number (Optional)
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact / Phone (Optional)") },
                        placeholder = { Text("e.g. 0300 1234567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Address (Optional)
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / City (Optional)") },
                        placeholder = { Text("e.g. Main Market, Lahore") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save & Get Started Button
            Button(
                onClick = {
                    if (shopName.trim().isBlank()) {
                        shopNameError = true
                    } else {
                        viewModel.saveShopSetup(
                            shopName = shopName,
                            logoUri = logoUri,
                            phone = phone,
                            address = address,
                            currency = "PKR",
                            defaultTax = 0.0
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("get_started_button"),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Generating Receipts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "You can change your shop profile anytime later.",
                fontSize = 11.5.sp,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
