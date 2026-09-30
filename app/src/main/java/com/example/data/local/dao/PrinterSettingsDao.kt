package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PrinterSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterSettingsDao {
    @Query("SELECT * FROM printer_settings WHERE id = 1 LIMIT 1")
    fun getPrinterSettings(): Flow<PrinterSettingsEntity?>

    @Query("SELECT * FROM printer_settings WHERE id = 1 LIMIT 1")
    suspend fun getPrinterSettingsDirect(): PrinterSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: PrinterSettingsEntity)

    @Update
    suspend fun update(settings: PrinterSettingsEntity)
}
