package com.example.data.repository

import com.example.data.local.dao.PrinterSettingsDao
import com.example.data.local.entity.PrinterSettingsEntity
import kotlinx.coroutines.flow.Flow

class PrinterRepository(private val printerSettingsDao: PrinterSettingsDao) {
    val printerSettings: Flow<PrinterSettingsEntity?> = printerSettingsDao.getPrinterSettings()

    suspend fun getSettingsDirect(): PrinterSettingsEntity {
        return printerSettingsDao.getPrinterSettingsDirect() ?: PrinterSettingsEntity()
    }

    suspend fun savePrinterSettings(settings: PrinterSettingsEntity) {
        printerSettingsDao.insertOrUpdate(settings)
    }
}
