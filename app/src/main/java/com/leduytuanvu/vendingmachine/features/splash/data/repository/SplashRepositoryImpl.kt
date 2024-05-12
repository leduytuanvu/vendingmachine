package com.leduytuanvu.vendingmachine.features.splash.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor(
    private val localStorageDatasource: LocalStorageDatasource,
) : SplashRepository {

    override suspend fun isFileInitSetupExists() : Boolean {
        try {
            return localStorageDatasource.checkFileExists(pathFileInitSetup)
        } catch (e: Exception) {
            throw e
        }
    }

    @SuppressLint("HardwareIds")
    override suspend fun getAndroidId(context: Context): String {
        try {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getInitSetupFromLocal(): InitSetup {
        try {
            return localStorageDatasource.getDataFromPath(pathFileInitSetup)!!
        } catch (e: Exception) {
            throw e
        }
    }
}