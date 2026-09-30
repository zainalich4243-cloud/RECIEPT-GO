package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PrinterSettingsEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.ShopProfile
import com.example.data.model.ReceiptItem
import com.example.data.repository.PrinterRepository
import com.example.data.repository.ReceiptRepository
import com.example.data.repository.ShopRepository
import com.example.util.BluetoothPrinterManager
import com.example.util.DiscoveredPrinter
import com.example.util.PrinterConnectionStatus
import com.example.util.ReceiptExporter
import com.example.util.ThermalEscPosHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Screen {
    SETUP,
    HOME,
    CREATE_RECEIPT,
    RECEIPT_PREVIEW,
    RECEIPT_HISTORY,
    RECEIPT_DETAIL,
    SHOP_PROFILE,
    PRINTER
}

enum class DateFilter {
    ALL,
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH
}

data class ReceiptDraft(
    val id: Long = 0,
    val receiptNumber: String = "",
    val sequenceNumber: Long = 1L,
    val dateTime: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val paymentMethod: String = "Cash",
    val items: List<ReceiptItem> = listOf(
        ReceiptItem(name = "", quantity = 1.0, unit = "Pcs", unitPrice = 0.0)
    ),
    val discountType: String = "AMOUNT", // AMOUNT or PERCENT
    val discountValue: Double = 0.0,
    val taxType: String = "PERCENT", // AMOUNT or PERCENT
    val taxValue: Double = 0.0,
    val notes: String = "Thank You! Please Visit Again."
) {
    val subtotal: Double
        get() = items.filter { it.name.isNotBlank() && it.unitPrice > 0 }.sumOf { it.totalPrice }

    val discountCalculated: Double
        get() {
            if (discountValue <= 0) return 0.0
            return if (discountType == "PERCENT") {
                (subtotal * (discountValue.coerceIn(0.0, 100.0) / 100.0))
            } else {
                discountValue.coerceAtMost(subtotal)
            }
        }

    val taxCalculated: Double
        get() {
            if (taxValue <= 0) return 0.0
            val taxableBase = (subtotal - discountCalculated).coerceAtLeast(0.0)
            return if (taxType == "PERCENT") {
                taxableBase * (taxValue / 100.0)
            } else {
                taxValue
            }
        }

    val totalAmount: Double
        get() = ((subtotal - discountCalculated) + taxCalculated).coerceAtLeast(0.0)
}

class ReceiptViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val shopRepository = ShopRepository(db.shopProfileDao())
    val receiptRepository = ReceiptRepository(db.receiptDao())
    val printerRepository = PrinterRepository(db.printerSettingsDao())
    val bluetoothPrinterManager = BluetoothPrinterManager(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<Screen>()

    val shopProfile: StateFlow<ShopProfile?> = shopRepository.shopProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val printerSettings: StateFlow<PrinterSettingsEntity> = printerRepository.printerSettings
        .flatMapLatest { flowOfSettings(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrinterSettingsEntity())

    private fun flowOfSettings(entity: PrinterSettingsEntity?): kotlinx.coroutines.flow.Flow<PrinterSettingsEntity> {
        return kotlinx.coroutines.flow.flowOf(entity ?: PrinterSettingsEntity())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilter.ALL)
    val selectedDateFilter: StateFlow<DateFilter> = _selectedDateFilter.asStateFlow()

    val allReceipts: StateFlow<List<ReceiptEntity>> = combine(
        receiptRepository.allReceipts,
        _searchQuery,
        _selectedDateFilter
    ) { list, query, filter ->
        var filtered = list
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter {
                it.receiptNumber.lowercase().contains(q) ||
                it.customerName.lowercase().contains(q) ||
                it.itemsJson.lowercase().contains(q)
            }
        }
        filtered = when (filter) {
            DateFilter.ALL -> filtered
            DateFilter.TODAY -> {
                val start = getStartOfDay(0)
                filtered.filter { it.createdAt >= start }
            }
            DateFilter.YESTERDAY -> {
                val start = getStartOfDay(-1)
                val end = getStartOfDay(0)
                filtered.filter { it.createdAt in start until end }
            }
            DateFilter.THIS_WEEK -> {
                val start = getStartOfWeek()
                filtered.filter { it.createdAt >= start }
            }
            DateFilter.THIS_MONTH -> {
                val start = getStartOfMonth()
                filtered.filter { it.createdAt >= start }
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentReceipts: StateFlow<List<ReceiptEntity>> = receiptRepository.getRecentReceipts(4)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayReceiptsCount: StateFlow<Int> = receiptRepository.getTodayReceiptsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayTotalSales: StateFlow<Double> = receiptRepository.getTodayTotalSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalReceiptsCount: StateFlow<Int> = receiptRepository.totalReceiptsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSales: StateFlow<Double> = receiptRepository.totalSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Active Draft state
    private val _draft = MutableStateFlow(ReceiptDraft())
    val draft: StateFlow<ReceiptDraft> = _draft.asStateFlow()

    // Selected receipt for Preview / Details
    private val _selectedReceipt = MutableStateFlow<ReceiptEntity?>(null)
    val selectedReceipt: StateFlow<ReceiptEntity?> = _selectedReceipt.asStateFlow()

    // Toast / User feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        checkInitialSetup()
    }

    private fun checkInitialSetup() {
        viewModelScope.launch {
            val profile = shopRepository.getProfileDirect()
            if (profile == null || !profile.isSetupCompleted || profile.shopName.isBlank()) {
                _currentScreen.value = Screen.SETUP
            } else {
                _currentScreen.value = Screen.HOME
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            val prev = _screenHistory.removeAt(_screenHistory.size - 1)
            _currentScreen.value = prev
            true
        } else {
            if (_currentScreen.value != Screen.HOME) {
                _currentScreen.value = Screen.HOME
                true
            } else {
                false
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    fun startNewReceipt() {
        viewModelScope.launch {
            val profile = shopRepository.getProfileDirect() ?: ShopProfile()
            val maxSeq = receiptRepository.getMaxSequenceNumber()
            val nextSeq = maxOf(profile.nextReceiptSequence, maxSeq + 1)
            val prefix = profile.receiptPrefix.ifBlank { "RC-" }
            val receiptNo = String.format("%s%06d", prefix, nextSeq)

            val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            _draft.value = ReceiptDraft(
                id = 0,
                receiptNumber = receiptNo,
                sequenceNumber = nextSeq,
                dateTime = now,
                customerName = "",
                customerPhone = "",
                paymentMethod = "Cash",
                items = listOf(
                    ReceiptItem(name = "", quantity = 1.0, unit = "KG", unitPrice = 0.0)
                ),
                discountType = "AMOUNT",
                discountValue = 0.0,
                taxType = "PERCENT",
                taxValue = profile.defaultTaxPercent,
                notes = profile.defaultFooter.ifBlank { "Thank You! Please Visit Again." }
            )
            _selectedReceipt.value = null
            navigateTo(Screen.CREATE_RECEIPT)
        }
    }

    fun editDraftItem(index: Int, updated: ReceiptItem) {
        val currentItems = _draft.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = updated
            _draft.value = _draft.value.copy(items = currentItems)
        }
    }

    fun addDraftItem(name: String = "", qty: Double = 1.0, unit: String = "Pcs", price: Double = 0.0) {
        val currentItems = _draft.value.items.toMutableList()
        currentItems.add(ReceiptItem(name = name, quantity = qty, unit = unit, unitPrice = price))
        _draft.value = _draft.value.copy(items = currentItems)
    }

    fun removeDraftItem(index: Int) {
        val currentItems = _draft.value.items.toMutableList()
        if (currentItems.size > 1 && index in currentItems.indices) {
            currentItems.removeAt(index)
            _draft.value = _draft.value.copy(items = currentItems)
        } else if (currentItems.size == 1) {
            currentItems[0] = ReceiptItem(name = "", quantity = 1.0, unit = "Pcs", unitPrice = 0.0)
            _draft.value = _draft.value.copy(items = currentItems)
        }
    }

    fun updateDraftCustomer(name: String, phone: String = "") {
        _draft.value = _draft.value.copy(customerName = name, customerPhone = phone)
    }

    fun updateDraftDateTime(dateTime: String) {
        _draft.value = _draft.value.copy(dateTime = dateTime)
    }

    fun updateDraftPaymentMethod(method: String) {
        _draft.value = _draft.value.copy(paymentMethod = method)
    }

    fun updateDraftDiscount(type: String, value: Double) {
        _draft.value = _draft.value.copy(discountType = type, discountValue = value)
    }

    fun updateDraftTax(type: String, value: Double) {
        _draft.value = _draft.value.copy(taxType = type, taxValue = value)
    }

    fun updateDraftNotes(notes: String) {
        _draft.value = _draft.value.copy(notes = notes)
    }

    fun saveAndPreviewReceipt(onSuccess: (ReceiptEntity) -> Unit) {
        val draftVal = _draft.value
        val validItems = draftVal.items.filter { it.name.trim().isNotBlank() }

        if (validItems.isEmpty()) {
            showToast("Please add at least one product name")
            return
        }

        viewModelScope.launch {
            val profile = shopRepository.getProfileDirect() ?: ShopProfile()

            val entity = ReceiptEntity(
                id = draftVal.id,
                receiptNumber = draftVal.receiptNumber,
                sequenceNumber = draftVal.sequenceNumber,
                createdAt = System.currentTimeMillis(),
                formattedDateTime = draftVal.dateTime.ifBlank {
                    SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                },
                customerName = draftVal.customerName.trim(),
                customerPhone = draftVal.customerPhone.trim(),
                paymentMethod = draftVal.paymentMethod,
                subtotal = draftVal.subtotal,
                discountType = draftVal.discountType,
                discountValue = draftVal.discountValue,
                discountCalculated = draftVal.discountCalculated,
                taxType = draftVal.taxType,
                taxValue = draftVal.taxValue,
                taxCalculated = draftVal.taxCalculated,
                totalAmount = draftVal.totalAmount,
                currency = profile.currency.ifBlank { "PKR" },
                notes = draftVal.notes,
                itemsJson = ReceiptItem.listToJsonString(validItems),
                shopNameSnapshot = profile.shopName,
                shopPhoneSnapshot = profile.phone,
                shopAddressSnapshot = profile.address,
                shopLogoUriSnapshot = profile.logoUri,
                status = "COMPLETED"
            )

            val insertedId = receiptRepository.insertReceipt(entity)
            val savedEntity = entity.copy(id = insertedId)

            // Update shop next sequence
            val updatedProfile = profile.copy(
                nextReceiptSequence = maxOf(profile.nextReceiptSequence, draftVal.sequenceNumber + 1)
            )
            shopRepository.saveShopProfile(updatedProfile)

            _selectedReceipt.value = savedEntity
            showToast("Receipt ${savedEntity.receiptNumber} saved successfully!")
            onSuccess(savedEntity)
        }
    }

    fun selectReceiptForView(receipt: ReceiptEntity) {
        _selectedReceipt.value = receipt
        navigateTo(Screen.RECEIPT_DETAIL)
    }

    fun deleteReceipt(receiptId: Long, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            receiptRepository.deleteReceiptById(receiptId)
            showToast("Receipt deleted")
            if (_selectedReceipt.value?.id == receiptId) {
                _selectedReceipt.value = null
            }
            onDeleted()
        }
    }

    fun saveShopSetup(
        shopName: String,
        logoUri: String?,
        phone: String = "",
        address: String = "",
        tagline: String = "Create. Print. Share.",
        currency: String = "PKR",
        defaultTax: Double = 0.0,
        defaultFooter: String = "Thank you for your business! Please visit again.",
        receiptPrefix: String = "RC-"
    ) {
        if (shopName.trim().isBlank()) {
            showToast("Shop name is required")
            return
        }

        viewModelScope.launch {
            val existing = shopRepository.getProfileDirect() ?: ShopProfile()
            val updated = existing.copy(
                shopName = shopName.trim(),
                logoUri = logoUri,
                phone = phone.trim(),
                address = address.trim(),
                tagline = tagline.trim(),
                currency = currency.trim().ifBlank { "PKR" },
                currencySymbol = if (currency.trim() == "PKR") "₨" else "$",
                defaultTaxPercent = defaultTax,
                defaultFooter = defaultFooter.trim(),
                receiptPrefix = receiptPrefix.trim().ifBlank { "RC-" },
                isSetupCompleted = true
            )
            shopRepository.saveShopProfile(updated)
            showToast("Shop Profile Saved!")
            _currentScreen.value = Screen.HOME
        }
    }

    fun savePrinterSettings(
        paperSize: String,
        autoCut: Boolean,
        printCopies: Int,
        includeLogo: Boolean,
        includeBarcode: Boolean
    ) {
        viewModelScope.launch {
            val current = printerRepository.getSettingsDirect()
            val updated = current.copy(
                paperSize = paperSize,
                autoCut = autoCut,
                printCopies = printCopies,
                includeLogo = includeLogo,
                includeBarcode = includeBarcode
            )
            printerRepository.savePrinterSettings(updated)
            showToast("Printer settings saved")
        }
    }

    fun connectPrinter(printer: DiscoveredPrinter) {
        viewModelScope.launch {
            val success = bluetoothPrinterManager.connectToPrinter(printer.address, printer.name)
            if (success) {
                val current = printerRepository.getSettingsDirect()
                printerRepository.savePrinterSettings(
                    current.copy(
                        connectedPrinterName = printer.name,
                        connectedPrinterAddress = printer.address
                    )
                )
                showToast("Connected to ${printer.name}")
            } else {
                showToast("Failed to connect to ${printer.name}")
            }
        }
    }

    fun disconnectPrinter() {
        bluetoothPrinterManager.disconnect()
        viewModelScope.launch {
            val current = printerRepository.getSettingsDirect()
            printerRepository.savePrinterSettings(
                current.copy(
                    connectedPrinterName = null,
                    connectedPrinterAddress = null
                )
            )
            showToast("Printer disconnected")
        }
    }

    fun testPrint() {
        viewModelScope.launch {
            val settings = printerRepository.getSettingsDirect()
            if (bluetoothPrinterManager.connectionStatus.value != PrinterConnectionStatus.CONNECTED) {
                showToast("Please connect a Bluetooth thermal printer first")
                return@launch
            }
            val bytes = ThermalEscPosHelper.generateTestSlip(settings.paperSize)
            val success = bluetoothPrinterManager.sendBytes(bytes)
            if (success) {
                showToast("Test receipt sent to printer!")
            } else {
                showToast("Printing failed: Please check printer connection")
            }
        }
    }

    fun printThermalReceipt(receipt: ReceiptEntity) {
        viewModelScope.launch {
            val settings = printerRepository.getSettingsDirect()
            val profile = shopRepository.getProfileDirect()

            if (bluetoothPrinterManager.connectionStatus.value != PrinterConnectionStatus.CONNECTED) {
                showToast("No Bluetooth printer connected. Using System Print.")
                // Fallback to system print
                val pdf = ReceiptExporter.generateReceiptPdf(
                    getApplication(),
                    receipt,
                    profile,
                    settings.paperSize
                )
                if (pdf != null) {
                    withContext(Dispatchers.Main) {
                        ReceiptExporter.printViaSystemManager(getApplication(), pdf)
                    }
                }
                return@launch
            }

            val bytes = ThermalEscPosHelper.generateReceiptBytes(
                receipt = receipt,
                shopProfile = profile,
                paperSize = settings.paperSize,
                autoCut = settings.autoCut
            )
            val success = bluetoothPrinterManager.sendBytes(bytes)
            if (success) {
                showToast("Receipt printed successfully!")
            } else {
                showToast("Print error. Please check printer.")
            }
        }
    }

    fun exportReceiptAsPdf(context: Context, receipt: ReceiptEntity, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val settings = printerRepository.getSettingsDirect()
            val profile = shopRepository.getProfileDirect()
            val pdfFile = ReceiptExporter.generateReceiptPdf(
                context = context,
                receipt = receipt,
                shopProfile = profile,
                paperWidthType = settings.paperSize
            )
            withContext(Dispatchers.Main) {
                if (pdfFile != null) {
                    showToast("PDF exported: ${pdfFile.name}")
                    onComplete(pdfFile)
                } else {
                    showToast("Failed to export PDF")
                    onComplete(null)
                }
            }
        }
    }

    fun exportReceiptAsImage(context: Context, receipt: ReceiptEntity, onComplete: (File?) -> Unit) {
        viewModelScope.launch {
            val settings = printerRepository.getSettingsDirect()
            val profile = shopRepository.getProfileDirect()
            val imgFile = ReceiptExporter.generateReceiptImage(
                context = context,
                receipt = receipt,
                shopProfile = profile,
                paperWidthType = settings.paperSize
            )
            withContext(Dispatchers.Main) {
                if (imgFile != null) {
                    showToast("Image exported: ${imgFile.name}")
                    onComplete(imgFile)
                } else {
                    showToast("Failed to export image")
                    onComplete(null)
                }
            }
        }
    }

    fun shareReceiptFile(context: Context, receipt: ReceiptEntity, asImage: Boolean = false) {
        viewModelScope.launch {
            val settings = printerRepository.getSettingsDirect()
            val profile = shopRepository.getProfileDirect()
            val file = if (asImage) {
                ReceiptExporter.generateReceiptImage(context, receipt, profile, settings.paperSize)
            } else {
                ReceiptExporter.generateReceiptPdf(context, receipt, profile, settings.paperSize)
            }
            withContext(Dispatchers.Main) {
                if (file != null) {
                    val mime = if (asImage) "image/png" else "application/pdf"
                    ReceiptExporter.shareReceipt(context, file, mime, "Receipt - ${receipt.receiptNumber}")
                } else {
                    showToast("Failed to prepare receipt for sharing")
                }
            }
        }
    }

    private fun getStartOfDay(dayOffset: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
