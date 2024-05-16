package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse

data class SetupPaymentViewState (
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
    val cashBoxData: String = "",
    val vendingMachineData: String = "",
)