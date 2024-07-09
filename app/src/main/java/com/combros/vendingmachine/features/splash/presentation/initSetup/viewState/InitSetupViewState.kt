package com.combros.vendingmachine.features.splash.presentation.initSetup.viewState

data class InitSetupViewState (
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogWarning: String = "",
    val androidId: String = "",
)