package com.leduytuanvu.vendingmachine.features.home.presentation.viewState

import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot

data class HomeViewState (
    val isLoading: Boolean = false,
    val isShowAds: Boolean = true,
    val isPayment: Boolean = false,
    val isPaymentConfirmation: Boolean = false,
    val nameMethodPayment: String = "cash",
    val voucherCode: String = "",
    val countDownPaymentByCash: Long = 0,
    val cashBoxData: ByteArray = byteArrayOf(),
    val vendingMachineData: ByteArray = byteArrayOf(),
    val initSetup: InitSetup? = null,
    val slotAtBottom: Slot? = null,
    val listAds: ArrayList<String> = arrayListOf(),
    val listSlot: ArrayList<Slot> = arrayListOf(),
    val listSlotInCard: ArrayList<Slot> = arrayListOf(),
    val listSlotInHome: ArrayList<Slot> = arrayListOf(),
    val listPaymentMethod: ArrayList<PaymentMethodResponse> = arrayListOf(),
    val promotion: PromotionResponse? = null,
    val totalAmount: Int = 0,
)