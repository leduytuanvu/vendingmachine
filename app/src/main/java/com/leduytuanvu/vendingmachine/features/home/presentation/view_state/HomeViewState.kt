package com.leduytuanvu.vendingmachine.features.home.presentation.view_state

import com.leduytuanvu.vendingmachine.core.errors.CustomError

data class HomeViewState (
    val isLoading: Boolean = false,
    val error: CustomError? = null,
)