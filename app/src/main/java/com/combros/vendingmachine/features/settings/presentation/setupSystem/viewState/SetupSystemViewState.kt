package com.combros.vendingmachine.features.settings.presentation.setupSystem.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse

data class SetupSystemViewState (
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogWarning: String = "",
    val isConfirm: Boolean = false,
    val titleDialogConfirm: String = "",
    val initSetup: InitSetup? = null,
    val informationOfMachine: DataInformationMachineResponse? = null,
    val serialSimId: String = "",
    val temp1: String = "",
    val temp2: String = "",
)