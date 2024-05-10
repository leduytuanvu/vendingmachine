package com.leduytuanvu.vendingmachine.features.splash.data.repository

import com.leduytuanvu.vendingmachine.core.storage.LocalStorage
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor() : SplashRepository {
    private val localStorage = LocalStorage()

    override suspend fun fileInitSetupExists() : Boolean {
        try {
            return localStorage.checkFileExists(localStorage.fileInitSetup)
        } catch (e: Exception) {
            throw e
        }
    }
}