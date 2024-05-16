package com.leduytuanvu.vendingmachine.features.home.presentation.viewState

import com.leduytuanvu.vendingmachine.core.errors.CustomError
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

data class HomeViewState (
    val isLoading: Boolean = false,
    val isShowAds: Boolean = true,
    val slot: Slot? = null,
    val listAds: ArrayList<String> = arrayListOf(),
    val listSlot: ArrayList<Slot> = arrayListOf(),
    val listSlotBuy: ArrayList<Slot> = arrayListOf(),
    val listSlotShowInHome: ArrayList<Slot> = arrayListOf(),
    val numberProduct: Int = 0,
)