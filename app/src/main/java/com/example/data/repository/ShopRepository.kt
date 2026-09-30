package com.example.data.repository

import com.example.data.local.dao.ShopProfileDao
import com.example.data.local.entity.ShopProfile
import kotlinx.coroutines.flow.Flow

class ShopRepository(private val shopProfileDao: ShopProfileDao) {
    val shopProfile: Flow<ShopProfile?> = shopProfileDao.getShopProfile()

    suspend fun getProfileDirect(): ShopProfile? = shopProfileDao.getShopProfileDirect()

    suspend fun saveShopProfile(profile: ShopProfile) {
        shopProfileDao.insertOrUpdate(profile)
    }

    suspend fun incrementReceiptSequence() {
        shopProfileDao.incrementReceiptSequence()
    }
}
