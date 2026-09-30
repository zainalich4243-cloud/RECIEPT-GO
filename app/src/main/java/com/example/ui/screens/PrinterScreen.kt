package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ReceiptGoTopAppBar
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.TealAccent
import com.example.util.DiscoveredPrinter
import com.example.util.PrinterConnectionStatus
import com.example.viewmodel.ReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrinterScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val printerSettings by viewModel.printerSettings.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.bluetoothPrinterManager.connectionStatus.collectAsStateWithLifecycle()
    val connectedDeviceName by viewModel.bluetoothPrinterManager.connectedDeviceName.collectAsStateWithLifecycle()
    val errorMessage by viewModel.bluetoothPrinterManager.errorMessage.collectAsStateWithLifecycle()

    var pairedPrinters by remember { mutableStateOf<List<DiscoveredPrinter>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    // Bluetooth permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            pairedPrinters = viewModel.bluetoothPrinterManager.getPairedPrinters()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            pairedPrinters = viewModel.bluetoothPrinterManager.getPairedPrinters()
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Slate50,
        topBar = {
            ReceiptGoTopAppBar(
                title = "Thermal Printer",
                subtitle = "Bluetooth & POS setup",
                onBackClick = { viewModel.navigateBack() }
            )
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

            // Connection Status Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (connectionStatus) {
                            PrinterConnectionStatus.CONNECTED -> EmeraldGreen.copy(alpha = 0.1f)
                            PrinterConnectionStatus.CONNECTING -> TealAccent.copy(alpha = 0.1f)
                            PrinterConnectionStatus.ERROR -> RoseError.copy(alpha = 0.1f)
                            PrinterConnectionStatus.DISCONNECTED -> Color.White
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        when (connectionStatus) {
                                            PrinterConnectionStatus.CONNECTED -> EmeraldGreen
                                            PrinterConnectionStatus.CONNECTING -> TealAccent
                                            PrinterConnectionStatus.ERROR -> RoseError
                                            PrinterConnectionStatus.DISCONNECTED -> Slate500
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (connectionStatus == PrinterConnectionStatus.CONNECTED) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = when (connectionStatus) {
                                        PrinterConnectionStatus.CONNECTED -> "Printer Connected"
                                        PrinterConnectionStatus.CONNECTING -> "Connecting..."
                                        PrinterConnectionStatus.ERROR -> "Connection Error"
                                        PrinterConnectionStatus.DISCONNECTED -> "No Printer Connected"
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = connectedDeviceName ?: (errorMessage ?: "Select a Bluetooth printer below"),
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                        }

                        if (connectionStatus == PrinterConnectionStatus.CONNECTED) {
                            OutlinedButton(
                                onClick = { viewModel.disconnectPrinter() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("disconnect_printer_button")
                            ) {
                                Text("Disconnect", fontSize = 11.sp, color = RoseError)
                            }
                        }
                    }
                }
            }

            // Paper Size Selection (58mm vs 80mm)
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
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Printer Paper Width",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Choose paper size matching your thermal roll",
                            fontSize = 12.sp,
                            color = Slate500,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = printerSettings.paperSize == "58mm",
                                onClick = {
                                    viewModel.savePrinterSettings(
                                        paperSize = "58mm",
                                        autoCut = printerSettings.autoCut,
                                        printCopies = printerSettings.printCopies,
                                        includeLogo = printerSettings.includeLogo,
                                        includeBarcode = printerSettings.includeBarcode
                                    )
                                },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("58mm (2 Inch)", fontWeight = FontWeight.Bold)
                                        Text("32 cols • Standard Mobile POS", fontSize = 10.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("paper_size_58mm")
                            )

                            FilterChip(
                                selected = printerSettings.paperSize == "80mm",
                                onClick = {
                                    viewModel.savePrinterSettings(
                                        paperSize = "80mm",
                                        autoCut = printerSettings.autoCut,
                                        printCopies = printerSettings.printCopies,
                                        includeLogo = printerSettings.includeLogo,
                                        includeBarcode = printerSettings.includeBarcode
                                    )
                                },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("80mm (3 Inch)", fontWeight = FontWeight.Bold)
                                        Text("48 cols • Desktop POS", fontSize = 10.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NavyPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("paper_size_80mm")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Slate100)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Auto cut switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Auto Paper Feed & Cut",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate900
                                )
                                Text(
                                    text = "Send ESC/POS cut feed commands after receipt",
                                    fontSize = 11.5.sp,
                                    color = Slate500
                                )
                            }
                            Switch(
                                checked = printerSettings.autoCut,
                                onCheckedChange = {
                                    viewModel.savePrinterSettings(
                                        paperSize = printerSettings.paperSize,
                                        autoCut = it,
                                        printCopies = printerSettings.printCopies,
                                        includeLogo = printerSettings.includeLogo,
                                        includeBarcode = printerSettings.includeBarcode
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NavyPrimary
                                )
                            )
                        }
                    }
                }
            }

            // TEST PRINT ACTION BUTTON
            item {
                Button(
                    onClick = { viewModel.testPrint() },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("test_print_button")
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Print Test Receipt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Paired & Nearby Bluetooth Printers List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paired Bluetooth Printers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    IconButton(
                        onClick = {
                            pairedPrinters = viewModel.bluetoothPrinterManager.getPairedPrinters()
                        },
                        modifier = Modifier.testTag("refresh_printers_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Slate700)
                    }
                }
            }

            if (pairedPrinters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = null,
                                tint = Slate500,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No paired Bluetooth printers found",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Slate700
                            )
                            Text(
                                text = "Please pair your Bluetooth printer in Android Settings first, then tap Refresh.",
                                fontSize = 11.5.sp,
                                color = Slate500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Bluetooth Settings", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(pairedPrinters) { printer ->
                    val isConnected = connectionStatus == PrinterConnectionStatus.CONNECTED &&
                            connectedDeviceName == printer.name

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.connectPrinter(printer) }
                            .testTag("printer_item_${printer.name}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConnected) EmeraldGreen.copy(alpha = 0.08f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(if (isConnected) EmeraldGreen else Slate100, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = null,
                                        tint = if (isConnected) Color.White else Slate600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = printer.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate900
                                    )
                                    Text(
                                        text = printer.address,
                                        fontSize = 11.sp,
                                        color = Slate500
                                    )
                                }
                            }

                            if (isConnected) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Connected",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.connectPrinter(printer) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Connect", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
