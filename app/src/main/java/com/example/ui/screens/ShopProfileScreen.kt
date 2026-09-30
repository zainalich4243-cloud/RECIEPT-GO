package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.components.ReceiptGoTopAppBar
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.viewmodel.ReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopProfileScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val currentProfile by viewModel.shopProfile.collectAsStateWithLifecycle()

    var shopName by remember { mutableStateOf(currentProfile?.shopName ?: "") }
    var phone by remember { mutableStateOf(currentProfile?.phone ?: "") }
    var address by remember { mutableStateOf(currentProfile?.address ?: "") }
    var tagline by remember { mutableStateOf(currentProfile?.tagline ?: "Create. Print. Share.") }
    var logoUri by remember { mutableStateOf<String?>(currentProfile?.logoUri) }
    var currency by remember { mutableStateOf(currentProfile?.currency ?: "PKR") }
    var defaultTax by remember { mutableStateOf(currentProfile?.defaultTaxPercent?.toString() ?: "0.0") }
    var defaultFooter by remember { mutableStateOf(currentProfile?.defaultFooter ?: "Thank You! Please Visit Again.") }
    var receiptPrefix by remember { mutableStateOf(currentProfile?.receiptPrefix ?: "RC-") }

    var shopNameError by remember { mutableStateOf(false) }

    LaunchedEffect(currentProfile) {
        if (currentProfile != null) {
            shopName = currentProfile!!.shopName
            phone = currentProfile!!.phone
            address = currentProfile!!.address
            tagline = currentProfile!!.tagline
            logoUri = currentProfile!!.logoUri
            currency = currentProfile!!.currency
            defaultTax = currentProfile!!.defaultTaxPercent.toString()
            defaultFooter = currentProfile!!.defaultFooter
            receiptPrefix = currentProfile!!.receiptPrefix
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

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
        containerColor = Slate50,
        topBar = {
            ReceiptGoTopAppBar(
                title = "Shop Profile",
                subtitle = "Manage business details",
                onBackClick = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (shopName.trim().isBlank()) {
                                shopNameError = true
                                viewModel.showToast("Shop name is required")
                            } else {
                                viewModel.saveShopSetup(
                                    shopName = shopName,
                                    logoUri = logoUri,
                                    phone = phone,
                                    address = address,
                                    tagline = tagline,
                                    currency = currency,
                                    defaultTax = defaultTax.toDoubleOrNull() ?: 0.0,
                                    defaultFooter = defaultFooter,
                                    receiptPrefix = receiptPrefix
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_shop_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .border(1.5.dp, if (logoUri != null) TealAccent else Slate200, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("edit_logo_button"),
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
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Change Logo",
                                tint = Slate500,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (logoUri != null) "Change Logo" else "Add Shop Logo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealAccent
                        )
                        if (logoUri != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { logoUri = null },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Slate500, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Shop Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Business Information",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = shopName,
                        onValueChange = {
                            shopName = it
                            if (it.isNotBlank()) shopNameError = false
                        },
                        label = { Text("Shop Name *") },
                        leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = NavyPrimary) },
                        isError = shopNameError,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_shop_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact / Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_shop_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Shop Address / City") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_shop_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline / Slogan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Receipt & Billing Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Receipt Defaults",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            label = { Text("Currency") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = receiptPrefix,
                            onValueChange = { receiptPrefix = it },
                            label = { Text("Prefix (e.g. RC-)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = defaultTax,
                        onValueChange = { defaultTax = it },
                        label = { Text("Default Tax / GST %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = defaultFooter,
                        onValueChange = { defaultFooter = it },
                        label = { Text("Default Footer Message") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Slate200
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
