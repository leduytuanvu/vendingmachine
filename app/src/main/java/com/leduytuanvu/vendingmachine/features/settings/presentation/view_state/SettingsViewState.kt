package com.leduytuanvu.vendingmachine.features.settings.presentation.view_state

import androidx.compose.ui.graphics.ImageBitmap
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.common.models.LogException
import com.leduytuanvu.vendingmachine.core.errors.CustomError
//import com.leduytuanvu.vendingmachine.core.room.LogException
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

data class SettingsViewState (
    val isLoading: Boolean = false,
    val isChooseNumber: Boolean = false,
    val isChooseMoney: Boolean = false,
    val isChooseImage: Boolean = false,
    val isConfirm: Boolean = false,
    val titleConfirm: String = "",
    val listSlot: ArrayList<Slot> = arrayListOf(),
    val listLogException: ArrayList<LogException> = arrayListOf(),
    val listSlotAddMore: ArrayList<Slot> = arrayListOf(),
    val listProduct: ArrayList<Product> = arrayListOf(),
    val listImageProduct: ArrayList<ImageBitmap> = arrayListOf(),
    val slot: Slot? = null,
    val isInventory: Boolean = false,
    val isCapacity: Boolean = false,
    val nameFunction: String = "",
    val error: CustomError? = null,
)