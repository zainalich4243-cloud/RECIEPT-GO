package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ShopProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopProfileDao {
    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    fun getShopProfile(): Flow<ShopProfile?>

    @Query("SELECT * FROM shop_profile WHERE id = 1 LIMIT 1")
    suspend fun getShopProfileDirect(): ShopProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ShopProfile)

    @Update
    suspend fun update(profile: ShopProfile)

    @Query("UPDATE shop_profile SET nextReceiptSequence = nextReceiptSequence + 1 WHERE id = 1")
    suspend fun incrementReceiptSequence()
}
