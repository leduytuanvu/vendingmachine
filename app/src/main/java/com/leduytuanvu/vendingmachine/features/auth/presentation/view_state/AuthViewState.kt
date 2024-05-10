package com.leduytuanvu.vendingmachine.features.auth.presentation.view_state

import com.leduytuanvu.vendingmachine.core.errors.CustomError

data class AuthViewState (
    val isLoading: Boolean = false,
    val error: CustomError? = null,
)