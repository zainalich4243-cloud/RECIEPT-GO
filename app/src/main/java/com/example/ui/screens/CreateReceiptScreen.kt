package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReceiptItem
import com.example.ui.components.ReceiptGoTopAppBar
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReceiptScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()
    val currency = shopProfile?.currency ?: "PKR"

    BackHandler {
        viewModel.navigateBack()
    }

    // Quick suggestion presets for ultra-fast shopkeeper entry
    val quickItems = listOf(
        "Sugar" to ("KG" to 160.0),
        "Milk" to ("L" to 180.0),
        "Rice" to ("KG" to 320.0),
        "Cooking Oil" to ("L" to 480.0),
        "Flour (Atta)" to ("KG" to 140.0),
        "Tea / Chai" to ("Pack" to 220.0),
        "Eggs" to ("Doz" to 280.0),
        "Bread" to ("Pcs" to 120.0),
        "Soap" to ("Pcs" to 95.0),
        "Cold Drink" to ("Bottle" to 150.0)
    )

    val paymentMethods = listOf("Cash", "Card", "EasyPaisa", "JazzCash", "Online", "Credit/Unpaid")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        containerColor = Slate50,
        topBar = {
            ReceiptGoTopAppBar(
                title = "New Receipt",
                subtitle = draft.receiptNumber,
                onBackClick = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            // Persistent Bottom Action Bar with Real-time Total
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL AMOUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                        Text(
                            text = "$currency ${formatNumber(draft.totalAmount)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyPrimary,
                            modifier = Modifier.testTag("draft_live_total")
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.saveAndPreviewReceipt { savedEntity ->
                                viewModel.selectReceiptForView(savedEntity)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("preview_and_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save & Preview",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Top Metadata Card: Receipt Number & Date
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
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Receipt Number",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                                Text(
                                    text = draft.receiptNumber,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.testTag("receipt_number_display")
                                )
                            }

                            // Date & Time picker button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                cal.set(Calendar.YEAR, year)
                                                cal.set(Calendar.MONTH, month)
                                                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                                TimePickerDialog(
                                                    context,
                                                    { _, hourOfDay, minute ->
                                                        cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                        cal.set(Calendar.MINUTE, minute)
                                                        val formatted = SimpleDateFormat(
                                                            "dd MMM yyyy, hh:mm a",
                                                            Locale.getDefault()
                                                        ).format(cal.time)
                                                        viewModel.updateDraftDateTime(formatted)
                                                    },
                                                    cal.get(Calendar.HOUR_OF_DAY),
                                                    cal.get(Calendar.MINUTE),
                                                    false
                                                ).show()
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                                    .testTag("edit_date_time_button"),
                                color = Slate100,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Edit Date",
                                        tint = Slate600,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = draft.dateTime.ifBlank { "Set Date" },
                                        fontSize = 11.5.sp,
                                        color = Slate700,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Customer Name & Phone
                        OutlinedTextField(
                            value = draft.customerName,
                            onValueChange = { viewModel.updateDraftCustomer(it, draft.customerPhone) },
                            label = { Text("Customer Name (Optional)") },
                            placeholder = { Text("e.g. Walk-in / Ahmed Ali") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Slate500)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate200,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Quick Product Preset Chips
            item {
                Column {
                    Text(
                        text = "Quick Product Suggestions (Tap to Add)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickItems) { (name, unitAndPrice) ->
                            val (unit, price) = unitAndPrice
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.addDraftItem(name = name, qty = 1.0, unit = unit, price = price)
                                    }
                                    .testTag("quick_item_$name"),
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "+ $name",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate900
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($currency ${price.toInt()})",
                                        fontSize = 10.5.sp,
                                        color = TealAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Products Table Header & Line Items
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Products Table",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Button(
                        onClick = { viewModel.addDraftItem() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealAccent.copy(alpha = 0.12f),
                            contentColor = TealAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_product_row_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Line Items List
            itemsIndexed(draft.items) { index, item ->
                ProductRowCard(
                    index = index,
                    item = item,
                    currency = currency,
                    onUpdate = { updated -> viewModel.editDraftItem(index, updated) },
                    onDelete = { viewModel.removeDraftItem(index) },
                    isOnlyItem = draft.items.size == 1
                )
            }

            // Financial Adjustments Card: Discount, Tax, Payment Method
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
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Discount & Tax Options",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Discount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = if (draft.discountValue == 0.0) "" else draft.discountValue.toString(),
                                onValueChange = {
                                    val num = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateDraftDiscount(draft.discountType, num)
                                },
                                label = { Text("Discount (${if (draft.discountType == "PERCENT") "%" else currency})") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("discount_value_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Slate200
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Toggle % vs Flat PKR
                            Row {
                                FilterChip(
                                    selected = draft.discountType == "AMOUNT",
                                    onClick = { viewModel.updateDraftDiscount("AMOUNT", draft.discountValue) },
                                    label = { Text(currency, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = draft.discountType == "PERCENT",
                                    onClick = { viewModel.updateDraftDiscount("PERCENT", draft.discountValue) },
                                    label = { Text("%", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tax Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = if (draft.taxValue == 0.0) "" else draft.taxValue.toString(),
                                onValueChange = {
                                    val num = it.toDoubleOrNull() ?: 0.0
                                    viewModel.updateDraftTax(draft.taxType, num)
                                },
                                label = { Text("Tax / GST (${if (draft.taxType == "PERCENT") "%" else currency})") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tax_value_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    unfocusedBorderColor = Slate200
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Quick tax presets (0%, 5%, 17%, 18%)
                            Row {
                                FilterChip(
                                    selected = draft.taxValue == 0.0,
                                    onClick = { viewModel.updateDraftTax("PERCENT", 0.0) },
                                    label = { Text("0%", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FilterChip(
                                    selected = draft.taxValue == 17.0,
                                    onClick = { viewModel.updateDraftTax("PERCENT", 17.0) },
                                    label = { Text("17%", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Payment Method Selection
                        Text(
                            text = "Payment Method",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(paymentMethods) { method ->
                                FilterChip(
                                    selected = draft.paymentMethod == method,
                                    onClick = { viewModel.updateDraftPaymentMethod(method) },
                                    label = { Text(method, fontSize = 12.sp) },
                                    leadingIcon = if (draft.paymentMethod == method) {
                                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NavyPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("payment_method_$method")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Footer note / terms
                        OutlinedTextField(
                            value = draft.notes,
                            onValueChange = { viewModel.updateDraftNotes(it) },
                            label = { Text("Footer Note / Terms") },
                            placeholder = { Text("Thank You! Please Visit Again.") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Calculation Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 13.sp, color = Slate600)
                            Text("$currency ${formatNumber(draft.subtotal)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        }

                        if (draft.discountCalculated > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount", fontSize = 13.sp, color = Color(0xFFE11D48))
                                Text("- $currency ${formatNumber(draft.discountCalculated)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE11D48))
                            }
                        }

                        if (draft.taxCalculated > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax / GST", fontSize = 13.sp, color = Slate600)
                                Text("+ $currency ${formatNumber(draft.taxCalculated)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET TOTAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(
                                text = "$currency ${formatNumber(draft.totalAmount)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProductRowCard(
    index: Int,
    item: ReceiptItem,
    currency: String,
    onUpdate: (ReceiptItem) -> Unit,
    onDelete: () -> Unit,
    isOnlyItem: Boolean
) {
    val units = listOf("Pcs", "KG", "L", "Pack", "Box", "Doz", "Item")
    var unitMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_row_$index"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Product Name & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onUpdate(item.copy(name = it)) },
                    placeholder = { Text("Product name (e.g. Sugar)") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_name_input_$index"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (!isOnlyItem) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .testTag("delete_item_button_$index")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove Item",
                            tint = Color(0xFFE11D48),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity, Unit & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity input
                OutlinedTextField(
                    value = if (item.quantity == 0.0) "" else if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity.toString(),
                    onValueChange = {
                        val q = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(quantity = q))
                    },
                    label = { Text("Qty") },
                    placeholder = { Text("1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("product_qty_input_$index"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Unit selector dropdown
                Box(modifier = Modifier.weight(0.9f)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { unitMenuExpanded = true }
                            .testTag("product_unit_dropdown_$index"),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Unit", fontSize = 10.sp, color = Slate500)
                            Text(
                                text = item.unit,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = unitMenuExpanded,
                        onDismissRequest = { unitMenuExpanded = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    onUpdate(item.copy(unit = u))
                                    unitMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price input
                OutlinedTextField(
                    value = if (item.unitPrice == 0.0) "" else if (item.unitPrice % 1.0 == 0.0) item.unitPrice.toInt().toString() else item.unitPrice.toString(),
                    onValueChange = {
                        val p = it.toDoubleOrNull() ?: 0.0
                        onUpdate(item.copy(unitPrice = p))
                    },
                    label = { Text("Price ($currency)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("product_price_input_$index"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        unfocusedBorderColor = Slate200
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Row Total Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "= $currency ${formatNumber(item.totalPrice)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent
                )
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
