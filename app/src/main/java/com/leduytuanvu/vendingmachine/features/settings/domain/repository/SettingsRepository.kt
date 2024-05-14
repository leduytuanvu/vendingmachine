package com.leduytuanvu.vendingmachine.features.settings.domain.repository

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface SettingsRepository {
    suspend fun getListSlotFromLocal() : ArrayList<Slot>
    suspend fun getListLayoutFromServer() : ArrayList<Slot>
    suspend fun getListProductFromServer() : ArrayList<Product>
    suspend fun getListProductFromLocal() : ArrayList<Product>
    suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>) : Boolean
    suspend fun getProductByCodeFromLocal(productCode: String) : Product?
    suspend fun getInformationOfMachine() : DataInformationMachineResponse
    suspend fun getSerialSimId() : String
    suspend fun getListFileNameInFolder(folderPath: String) : ArrayList<String>
}