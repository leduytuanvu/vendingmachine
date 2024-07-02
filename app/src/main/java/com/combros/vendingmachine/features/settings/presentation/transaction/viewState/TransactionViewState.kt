package com.combros.vendingmachine.features.settings.presentation.transaction.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.model.LogSyncOrder
import com.combros.vendingmachine.common.base.domain.model.LogsLocal
import com.combros.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
import com.combros.vendingmachine.features.settings.domain.model.Slot

data class TransactionViewState (
    val isLoading: Boolean = false,
    val isConfirm: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogConfirm: String = "",
    val typeConfirm: String = "",
    val sessionId: String = "",
    val titleDialogWarning: String = "",
    val slot: Slot? = null,
    val nameFunction: String = "",
    val informationOfMachine: DataInformationMachineResponse? = null,
    val initSetup: InitSetup? = null,
    val listLogServerLocal: ArrayList<LogsLocal> = arrayListOf(),
    val countTransactionByCash: Int = 0,
    val amountTransactionByCash: Int = 0,
    val countTransactionByOnline: Int = 0,
    val amountTransactionByOnline: Int = 0,
    val numberRottenBoxBalance: Int = 0,
    val listSyncOrder: ArrayList<LogSyncOrder> = arrayListOf()
)