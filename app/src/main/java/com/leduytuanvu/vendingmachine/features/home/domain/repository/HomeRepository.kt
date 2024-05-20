package com.leduytuanvu.vendingmachine.features.home.domain.repository

import android.content.Context
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface HomeRepository {
    suspend fun getListVideoAdsFromLocal() : ArrayList<String>
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

    suspend fun getTotalAmount(listSlot: ArrayList<Slot>): Int
}