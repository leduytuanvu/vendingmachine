package com.combros.vendingmachine.features.settings.presentation.settings.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.model.LogsLocal
import com.combros.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
//import com.leduytuanvu.vendingmachine.core.room.LogException
import com.combros.vendingmachine.features.settings.domain.model.Slot

data class SettingsViewState (
    val isLoading: Boolean = false,
//    val isChooseNumber: Boolean = false,
//    val isChooseMoney: Boolean = false,
//    val isChooseImage: Boolean = false,
    val isConfirm: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogConfirm: String = "",
    val titleDialogWarning: String = "",
//    val listSlot: ArrayList<Slot> = arrayListOf(),
//    val listSlotAddMore: ArrayList<Slot> = arrayListOf(),
//    val listProduct: ArrayList<Product> = arrayListOf(),
//    val listImageProduct: ArrayList<ImageBitmap> = arrayListOf(),
    val slot: Slot? = null,
//    val isInventory: Boolean = false,
//    val isCapacity: Boolean = false,
    val nameFunction: String = "",
//    val serialSimId: String = "",
//    val error: CustomError? = null,
    val informationOfMachine: DataInformationMachineResponse? = null,
    val initSetup: InitSetup? = null,
    val listLogServerLocal: ArrayList<LogsLocal> = arrayListOf(),
    val countTransactionByCash: Int = 0,
    val amountTransactionByCash: Int = 0,
    val countTransactionByOnline: Int = 0,
    val amountTransactionByOnline: Int = 0,
)