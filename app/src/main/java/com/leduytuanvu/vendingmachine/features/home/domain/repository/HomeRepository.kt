package com.leduytuanvu.vendingmachine.features.home.domain.repository

import android.content.Context
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

interface HomeRepository {
    fun getListVideoAdsFromLocal() : ArrayList<String>
    fun writeVideoAdsFromAssetToLocal(
        context: Context,
        rawResId: Int,
        fileName: String,
        pathFolderAds: String,
    )
}