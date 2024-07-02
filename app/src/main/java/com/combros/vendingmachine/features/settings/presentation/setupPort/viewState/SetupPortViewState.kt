package com.combros.vendingmachine.features.settings.presentation.setupPort.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup

data class SetupPortViewState(
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
)