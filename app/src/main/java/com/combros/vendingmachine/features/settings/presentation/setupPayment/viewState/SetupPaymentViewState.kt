package com.combros.vendingmachine.features.settings.presentation.setupPayment.viewState

import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.features.settings.data.model.response.PaymentMethodResponse

data class SetupPaymentViewState (
    val isLoading: Boolean = false,
    val isWarning: Boolean = false,
    val initSetup: InitSetup? = null,
    val cashBoxData: String = "",
    val numberRottenBoxBalance: Int = 0,
    val vendingMachineData: String = "",
    val titleDialogWarning: String = "",
    val listPaymentMethod: ArrayList<PaymentMethodResponse> = arrayListOf(),
    val putMoneyInTheRottenBox: Boolean = false,
)