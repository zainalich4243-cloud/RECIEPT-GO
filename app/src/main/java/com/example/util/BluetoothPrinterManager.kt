package com.example.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

enum class PrinterConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class DiscoveredPrinter(
    val name: String,
    val address: String,
    val isPaired: Boolean = false
)

class BluetoothPrinterManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    private val _connectionStatus = MutableStateFlow(PrinterConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<PrinterConnectionStatus> = _connectionStatus

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val isBluetoothAvailable: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    // Standard SPP UUID for Bluetooth Serial / Thermal Printers
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<DiscoveredPrinter> {
        val list = mutableListOf<DiscoveredPrinter>()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            return list
        }
        try {
            val paired = bluetoothAdapter.bondedDevices
            paired?.forEach { device ->
                list.add(
                    DiscoveredPrinter(
                        name = device.name ?: "Unknown Printer",
                        address = device.address,
                        isPaired = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("BluetoothPrinter", "Error getting paired devices", e)
        }
        return list
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToPrinter(address: String, deviceName: String): Boolean = withContext(Dispatchers.IO) {
        if (bluetoothAdapter == null) {
            _errorMessage.value = "Bluetooth is not supported on this device"
            _connectionStatus.value = PrinterConnectionStatus.ERROR
            return@withContext false
        }

        if (!bluetoothAdapter.isEnabled) {
            _errorMessage.value = "Please enable Bluetooth"
            _connectionStatus.value = PrinterConnectionStatus.ERROR
            return@withContext false
        }

        _connectionStatus.value = PrinterConnectionStatus.CONNECTING
        _errorMessage.value = null

        disconnect()

        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            bluetoothAdapter.cancelDiscovery()

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket?.connect()
            outputStream = socket?.outputStream

            _connectionStatus.value = PrinterConnectionStatus.CONNECTED
            _connectedDeviceName.value = deviceName.ifBlank { device.name ?: address }
            return@withContext true
        } catch (e: Exception) {
            Log.e("BluetoothPrinter", "Connection failed", e)
            _connectionStatus.value = PrinterConnectionStatus.ERROR
            _errorMessage.value = "Failed to connect: ${e.localizedMessage ?: "Unknown error"}"
            disconnect()
            return@withContext false
        }
    }

    suspend fun sendBytes(data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        if (socket == null || socket?.isConnected != true || outputStream == null) {
            _errorMessage.value = "Printer is not connected"
            _connectionStatus.value = PrinterConnectionStatus.DISCONNECTED
            return@withContext false
        }

        try {
            outputStream?.write(data)
            outputStream?.flush()
            return@withContext true
        } catch (e: Exception) {
            Log.e("BluetoothPrinter", "Failed to send data", e)
            _errorMessage.value = "Print error: ${e.localizedMessage}"
            _connectionStatus.value = PrinterConnectionStatus.ERROR
            return@withContext false
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            socket?.close()
        } catch (_: Exception) {}
        outputStream = null
        socket = null
        _connectionStatus.value = PrinterConnectionStatus.DISCONNECTED
        _connectedDeviceName.value = null
    }
}
