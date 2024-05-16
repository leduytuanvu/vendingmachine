package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse

data class SetupSystemViewState (
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
    val informationOfMachine: DataInformationMachineResponse? = null,
    val serialSimId: String = "",
)