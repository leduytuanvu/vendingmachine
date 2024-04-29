package com.leduytuanvu.vendingmachine.features.splash.domain.repository

interface SplashRepository {
    suspend fun checkVendCodeExists() : Boolean
}