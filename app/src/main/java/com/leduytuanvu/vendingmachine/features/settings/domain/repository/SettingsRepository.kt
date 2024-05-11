package com.leduytuanvu.vendingmachine.features.settings.domain.repository

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface SettingsRepository {
    suspend fun initLoadSlotFromLocal() : ArrayList<Slot>
    suspend fun loadLayoutFromServer() : ArrayList<Slot>
    suspend fun loadProductFromServer() : ArrayList<Product>
    suspend fun loadListProductFromLocal() : ArrayList<Product>
    suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>) : Boolean
    suspend fun loadImageFromLocal(context: Context) : ArrayList<ImageBitmap>
    suspend fun getProductByCode(productCode: String) : Product?
    suspend fun getAndroidId(context: Context) : String
}