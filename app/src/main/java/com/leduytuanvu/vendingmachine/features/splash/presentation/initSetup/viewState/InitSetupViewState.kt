package com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.viewState

data class InitSetupViewState (
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogWarning: String = "",
)