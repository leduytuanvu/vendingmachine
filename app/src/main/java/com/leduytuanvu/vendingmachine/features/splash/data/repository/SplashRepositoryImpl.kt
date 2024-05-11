package com.leduytuanvu.vendingmachine.features.splash.data.repository

import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor() : SplashRepository {
    private val localStorageDatasource = LocalStorageDatasource()

    override suspend fun fileInitSetupExists() : Boolean {
        try {
            return localStorageDatasource.checkFileExists(localStorageDatasource.fileInitSetup)
        } catch (e: Exception) {
            throw e
        }
    }
}