package com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.ImageResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PriceResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product

data class SetupProductViewState (
    val isLoading: Boolean = false,
    val initSetup: InitSetup? = null,
    val isWarning: Boolean = false,
    val isConfirm: Boolean = false,
    val titleDialogWarning: String = "",
    val titleDialogConfirm: String = "",
    val listProduct: ArrayList<Product> = arrayListOf(),
    val listPriceOfProduct: ArrayList<PriceResponse> = arrayListOf(),
    val listPathImageOfProduct: ArrayList<ImageResponse> = arrayListOf(),
)