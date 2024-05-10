package com.leduytuanvu.vendingmachine.features.splash.presentation.view_state

import com.leduytuanvu.vendingmachine.core.errors.CustomError

data class SplashViewState (
    val isLoading: Boolean = false,
    val error: CustomError? = null,
    val isVendCodeExists: Boolean = false,
)