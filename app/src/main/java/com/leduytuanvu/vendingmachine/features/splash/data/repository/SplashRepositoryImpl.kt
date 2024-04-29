package com.leduytuanvu.vendingmachine.features.splash.data.repository

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.leduytuanvu.vendingmachine.core.errors.CustomError
import com.leduytuanvu.vendingmachine.core.storage.LocalStorage
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor() : SplashRepository {
    private val localStorage = LocalStorage()

    override suspend fun checkVendCodeExists() : Boolean {
        try {
            return localStorage.checkFileExists(localStorage.filePathVendCode)
        } catch (e: Exception) {
            throw e
        }
    }
}