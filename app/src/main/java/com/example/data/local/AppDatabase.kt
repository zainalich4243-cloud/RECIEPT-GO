package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.PrinterSettingsDao
import com.example.data.local.dao.ReceiptDao
import com.example.data.local.dao.ShopProfileDao
import com.example.data.local.entity.PrinterSettingsEntity
import com.example.data.local.entity.ReceiptEntity
import com.example.data.local.entity.ShopProfile

@Database(
    entities = [
        ShopProfile::class,
        ReceiptEntity::class,
        PrinterSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shopProfileDao(): ShopProfileDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun printerSettingsDao(): PrinterSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "receipt_go_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
