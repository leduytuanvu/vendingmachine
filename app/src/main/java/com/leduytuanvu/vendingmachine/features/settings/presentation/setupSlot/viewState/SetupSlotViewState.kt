package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

data class SetupSlotViewState (
    val isLoading: Boolean = false,
    val isConfirm: Boolean = false,
    val isChooseImage: Boolean = false,
    val isChooseNumber: Boolean = false,
    val isChooseMoney: Boolean = false,
    val isInventory: Boolean = false,
    val isCapacity: Boolean = false,
    val isWarning: Boolean = false,
    val titleDialogWarning: String = "",
    val titleDialogConfirm: String = "",
    val nameFunction: String = "",
    val numberSlot: String = "0",
    val slot: Slot? = null,
    val initSetup: InitSetup? = null,
    val listSlot: ArrayList<Slot> = arrayListOf(),
    val listSlotUpdateInventory: ArrayList<Slot> = arrayListOf(),
    val listProduct: ArrayList<Product> = arrayListOf(),
    val listSlotAddMore: ArrayList<Slot> = arrayListOf(),
)