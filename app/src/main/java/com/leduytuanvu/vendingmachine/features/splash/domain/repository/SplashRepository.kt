package com.leduytuanvu.vendingmachine.features.splash.domain.repository

import android.content.Context
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup

interface SplashRepository {
    suspend fun isFileInitSetupExists() : Boolean
    suspend fun getAndroidId(context: Context) : String
    suspend fun getInitSetupFromLocal() : InitSetup
}