package com.leduytuanvu.vendingmachine.features.home.data.repository

import android.content.Context
import com.google.gson.Gson
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFolderAds
import com.leduytuanvu.vendingmachine.features.home.domain.repository.HomeRepository
import com.leduytuanvu.vendingmachine.features.settings.data.remote.SettingsApi
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val localStorageDatasource: LocalStorageDatasource,
    private val logger: Logger,
) : HomeRepository {
    override fun getListVideoAdsFromLocal(): ArrayList<String> {
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

    override fun writeVideoAdsFromAssetToLocal(
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


}