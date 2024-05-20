package com.leduytuanvu.vendingmachine.features.home.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFolderAds
import com.leduytuanvu.vendingmachine.features.home.data.model.request.ItemPromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.PromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.DataPromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.remote.HomeApi
import com.leduytuanvu.vendingmachine.features.home.domain.repository.HomeRepository
import com.leduytuanvu.vendingmachine.features.settings.data.remote.SettingsApi
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
                vendCode = initSetup.vendCode,
                totalAmount = totalAmount,
                totalDiscount = 0,
                paymentAmount = totalAmount,
                voucherCode = voucherCode,
                carts = carts,
            )
            val response = homeApi.getPromotion(request)
            logger.debug("response: $response")
            return response.data
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


}