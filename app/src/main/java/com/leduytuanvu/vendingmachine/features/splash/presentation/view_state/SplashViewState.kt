package com.leduytuanvu.vendingmachine.features.splash.presentation.view_state

import com.leduytuanvu.vendingmachine.core.errors.CustomError

data class SplashViewState (
    val isLoading: Boolean = false,
    val isVendCodeExists: Boolean = false,
    val navigateTo: String = "",
    val error: CustomError? = null,
)