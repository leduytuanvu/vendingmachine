package com.leduytuanvu.vendingmachine.features.home.data.repository

import android.content.Context
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServer
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.pathFolderAds
import com.leduytuanvu.vendingmachine.core.util.pathFolderBigAds
import com.leduytuanvu.vendingmachine.features.home.data.model.request.CheckPaymentResultOnlineRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.GetQrCodeRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.ItemPromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.LogServerRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.PromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.DataSyncOrderRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.SyncOrderRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdateDeliveryStatusRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdateInventoryRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdatePromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.CheckPaymentResultOnlineResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.DepositAndWithdrawMoneyResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.GetQrCodeResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.LogServerResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.SyncOrderResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdateDeliveryStatusResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdateInventoryResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdatePromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.remote.HomeApi
import com.leduytuanvu.vendingmachine.features.home.domain.repository.HomeRepository
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val localStorageDatasource: LocalStorageDatasource,
    private val baseRepository: BaseRepository,
    private val homeApi: HomeApi,
    private val logger: Logger,
) : HomeRepository {
    override suspend  fun getListVideoAdsFromLocal(): ArrayList<String> {
        try {
            val listPathAds = localStorageDatasource.getListPathFileInFolder(pathFolderAds)
            for(item in listPathAds) {
                logger.info(item)
            }
            return listPathAds
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListVideoBigAdsFromLocal(): ArrayList<String> {
        try {
            val listPathAds = localStorageDatasource.getListPathFileInFolder(pathFolderBigAds)
            for(item in listPathAds) {
                logger.info(item)
            }
            return listPathAds
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend  fun writeVideoAdsFromAssetToLocal(
        context: Context,
        rawResId: Int,
        fileName: String,
        pathFolderAds: String
    ) {
        try {
            localStorageDatasource.writeVideoAdsFromAssetToLocal(
                context,
                rawResId,
                fileName,
                pathFolderAds,
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend  fun getPromotion(
        voucherCode: String,
        listSlot: ArrayList<Slot>,
    ): PromotionResponse {
        try {
            val initSetup: InitSetup = baseRepository.getDataFromLocal(
                type = object : TypeToken<InitSetup>() {}.type,
                path = pathFileInitSetup
            )!!
            val carts: ArrayList<ItemPromotionRequest> = arrayListOf()
            var totalAmount = 0
            for(item in listSlot) {
                totalAmount += (item.inventory*item.price)
                val itemCart = ItemPromotionRequest(
                    productCode = item.productCode,
                    productName = item.productName,
                    price = item.price,
                    quantity = item.inventory,
                    discount = 0,
                    amount = item.inventory*item.price,
                )
                carts.add(itemCart)
            }
            val request = PromotionRequest(
                machineCode = initSetup.vendCode,
                androidId = initSetup.androidId,
                totalAmount = totalAmount,
                totalDiscount = 0,
                paymentAmount = totalAmount,
                voucherCode = voucherCode,
                carts = carts,
            )
            val response = homeApi.getPromotion(request)
//            logger.debug("response neeeeeeeeeeeeeeeeeee: $response")
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updatePromotion(updatePromotionRequest: UpdatePromotionRequest): BaseResponse<UpdatePromotionResponse> {
        try {
            val response = homeApi.updatePromotion(updatePromotionRequest)
            return response
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getTotalAmount(listSlot: ArrayList<Slot>): Int {
        try {
            var totalAmount = 0
            for(item in listSlot) {
                totalAmount += (item.inventory*item.price)
            }
            return totalAmount
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getSlotDrop(productCode: String): Slot? {
        try {
//            logger.debug("product code: $productCode")
            val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                type = object : TypeToken<ArrayList<Slot>>() {}.type,
                path = pathFileSlot
            )!!
            var slot: Slot? = null
            var inventory = 0
            for(item in listSlot) {
                if(productCode == item.productCode && item.productCode.isNotEmpty() && !item.isLock && item.inventory>inventory) {
                    inventory = item.inventory
                    slot = item
                }
            }
            return slot
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun lockSlot(slotIndex: Int) {
        try {
            val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                type = object : TypeToken<ArrayList<Slot>>() {}.type,
                path = pathFileSlot
            )!!
            val index = listSlot.indexOfFirst { it.slot == slotIndex }
            if(index!=-1) {
                listSlot[index].isLock = true
                baseRepository.writeDataToLocal(listSlot, pathFileSlot)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun minusInventory(slotIndex: Int) {
        try {
            val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                type = object : TypeToken<ArrayList<Slot>>() {}.type,
                path = pathFileSlot
            )!!
            val index = listSlot.indexOfFirst { it.slot == slotIndex }
            if(index!=-1) {
                listSlot[index].inventory--
                baseRepository.writeDataToLocal(listSlot, pathFileSlot)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListAnotherSlot(productCode: String): ArrayList<Slot> {
        try {
            val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                type = object : TypeToken<ArrayList<Slot>>() {}.type,
                path = pathFileSlot
            )!!
            // Filter slots that match the productCode
            val matchingSlots = listSlot.filter {
                it.productCode == productCode && !it.isLock && it.inventory > 0
            }
//            for(item in ArrayList(matchingSlots)) {
//                Logger.debug("slot another found: $item")
//            }
            // Return the matching slots
            return ArrayList(matchingSlots)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun logMulti(listEvents: ArrayList<LogServer>): ArrayList<LogServerResponse> {
        try {
            val initSetup: InitSetup = baseRepository.getDataFromLocal(
                type = object : TypeToken<InitSetup>() {}.type,
                path = pathFileInitSetup
            )!!
            val logServerRequest = LogServerRequest(
                machineCode = initSetup.vendCode,
                androidId = initSetup.androidId,
                events = listEvents,
            )
            val response = homeApi.logMulti(logServerRequest)
//            logger.info(response.toString())
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun pushDepositWithdrawToServer(depositWithdrawRequest: DepositAndWithdrawMoneyRequest): DepositAndWithdrawMoneyResponse {
        try {
            val response = homeApi.depositAndWithdrawMoney(depositWithdrawRequest)
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getQrCodeFromServer(getQrCodeRequest: GetQrCodeRequest): GetQrCodeResponse {
        try {
            val response = homeApi.getQrCode(getQrCodeRequest)
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun checkResultPaymentOnline(checkPaymentResultOnlineRequest: CheckPaymentResultOnlineRequest): CheckPaymentResultOnlineResponse {
        try {
            val response = homeApi.checkResultPaymentOnline(checkPaymentResultOnlineRequest)
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateDeliveryStatus(updateDeliveryStatusRequest: UpdateDeliveryStatusRequest): BaseResponse<UpdateDeliveryStatusResponse> {
        try {
            val response = homeApi.updateDeliveryStatus(updateDeliveryStatusRequest)
            return response
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun syncOrder(syncOrderRequest: SyncOrderRequest): BaseResponse<SyncOrderResponse> {
        try {
            val response = homeApi.syncOrder(syncOrderRequest)
            return response
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateInventory(updateInventory: UpdateInventoryRequest): BaseListResponse<UpdateInventoryResponse> {
        try {
            val response = homeApi.updateMultiInventory(updateInventory)
            return response
        } catch (e: Exception) {
            throw e
        }
    }
}