package com.leduytuanvu.vendingmachine.features.settings.domain.repository

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface SettingsRepository {
    suspend fun getListSlotFromLocal() : ArrayList<Slot>
    suspend fun getListLayoutFromServer() : ArrayList<Slot>
    suspend fun getListProductFromServer() : ArrayList<Product>
    suspend fun getListProductFromLocal() : ArrayList<Product>
    suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>) : Boolean
    suspend fun writeInitSetupToLocal(initSetup: InitSetup) : Boolean
    suspend fun getListImageBitmapFromLocal(context: Context) : ArrayList<ImageBitmap>
    suspend fun getProductByCodeFromLocal(productCode: String) : Product?
    suspend fun getInformationOfMachine() : DataInformationMachineResponse
    suspend fun getSerialSimId(context: Context) : String
}