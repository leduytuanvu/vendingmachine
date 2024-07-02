package com.combros.vendingmachine.features.settings.presentation.setupProduct.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.features.settings.data.model.response.ImageResponse
import com.combros.vendingmachine.features.settings.data.model.response.PriceResponse
import com.combros.vendingmachine.features.settings.domain.model.Product

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