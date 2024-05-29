package com.leduytuanvu.vendingmachine.features.auth.presentation.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.errors.CustomError

data class AuthViewState (
    val isLoading: Boolean = false,
)