package com.leduytuanvu.vendingmachine.features.home.domain.repository

import android.content.Context
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServer
import com.leduytuanvu.vendingmachine.features.home.data.model.request.CheckPaymentResultOnlineRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.GetQrCodeRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.SyncOrderRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdateDeliveryStatusRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdatePromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.CheckPaymentResultOnlineResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.DepositAndWithdrawMoneyResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.GetQrCodeResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.LogServerResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.SyncOrderResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdateDeliveryStatusResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdatePromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdateInventoryRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdateInventoryResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface HomeRepository {
    suspend fun getListVideoAdsFromLocal() : ArrayList<String>
    suspend fun getListVideoBigAdsFromLocal() : ArrayList<String>
    suspend fun writeVideoAdsFromAssetToLocal(
        context: Context,
        rawResId: Int,
        fileName: String,
        pathFolderAds: String,
    )
    suspend fun getPromotion(
        voucherCode: String,
        listSlot: ArrayList<Slot>,
    ): PromotionResponse
    suspend fun updatePromotion(updatePromotionRequest: UpdatePromotionRequest): BaseResponse<UpdatePromotionResponse>

    suspend fun getTotalAmount(listSlot: ArrayList<Slot>): Int
    suspend fun getSlotDrop(productCode: String): Slot?
    suspend fun lockSlot(slotIndex: Int)
    suspend fun minusInventory(slotIndex: Int)
    suspend fun getListAnotherSlot(productCode: String): ArrayList<Slot>
    suspend fun logMulti(listEvents: ArrayList<LogServer>): ArrayList<LogServerResponse>
    suspend fun pushDepositWithdrawToServer(depositWithdrawRequest: DepositAndWithdrawMoneyRequest): DepositAndWithdrawMoneyResponse
    suspend fun getQrCodeFromServer(getQrCodeRequest: GetQrCodeRequest): GetQrCodeResponse
    suspend fun checkResultPaymentOnline(checkPaymentResultOnlineRequest: CheckPaymentResultOnlineRequest): CheckPaymentResultOnlineResponse
    suspend fun updateDeliveryStatus(updateDeliveryStatusRequest: UpdateDeliveryStatusRequest): BaseResponse<UpdateDeliveryStatusResponse>
    suspend fun syncOrder(syncOrderRequest: SyncOrderRequest): BaseResponse<SyncOrderResponse>
    suspend fun updateInventory(updateInventory: UpdateInventoryRequest): BaseListResponse<UpdateInventoryResponse>
}