package com.combros.vendingmachine.features.auth.presentation.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup

data class AuthViewState (
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
)