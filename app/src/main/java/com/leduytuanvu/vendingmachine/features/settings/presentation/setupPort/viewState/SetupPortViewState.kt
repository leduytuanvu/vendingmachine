package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup

data class SetupPortViewState(
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
)