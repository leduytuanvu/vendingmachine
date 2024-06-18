package com.leduytuanvu.vendingmachine.features.settings.domain.repository

import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdateInventoryRequest
import com.leduytuanvu.vendingmachine.features.settings.data.model.request.EndOfSessionRequest
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.ImageResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PriceResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.ProductInventoryResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface SettingsRepository {
    suspend fun getListSlotFromLocal() : ArrayList<Slot>
    suspend fun getListLayoutFromServer() : ArrayList<Slot>
    suspend fun getListProductFromServer() : ArrayList<Product>
    suspend fun getListPriceOfProductFromServer() : ArrayList<PriceResponse>
    suspend fun getListImageOfProductFromServer() : ArrayList<ImageResponse>
    suspend fun getListPaymentMethodFromServer() : ArrayList<PaymentMethodResponse>
    suspend fun getListProductFromLocal() : ArrayList<Product>
    suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>) : Boolean
    suspend fun getProductByCodeFromLocal(productCode: String) : Product?
    suspend fun getInformationOfMachine() : DataInformationMachineResponse
    suspend fun getSerialSimId() : String
    suspend fun getListFileNameInFolder(folderPath: String) : ArrayList<String>
    suspend fun endOfSession(endOfSessionRequest: EndOfSessionRequest) : BaseResponse<String>
}