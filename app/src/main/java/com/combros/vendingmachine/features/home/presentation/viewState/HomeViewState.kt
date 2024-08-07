package com.combros.vendingmachine.features.home.presentation.viewState

import androidx.compose.ui.graphics.ImageBitmap
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.model.LogSyncOrder
import com.combros.vendingmachine.features.home.data.model.response.PromotionResponse
import com.combros.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.combros.vendingmachine.features.settings.domain.model.Slot

data class HomeViewState (
    val isLoading: Boolean = false,
    val isReturning: Boolean = false,
    val isVendingMachineBusy: Boolean = false,
    val isShowAds: Boolean = true,
    val isShowCart: Boolean = false,
    val isConfirm: Boolean = false,
    val isShowBigAds: Boolean = false,
    val isShowQrCode: Boolean = false,
    val isShowPushMoney: Boolean = false,
    val isShowWaitForDropProduct: Boolean = false,
    val isWarning: Boolean = false,
    val isWithdrawMoney: Boolean = false,
    val titleDialogWarning: String = "",
    val titleDialogConfirm: String = "",
    val setUpCashBox: Boolean = false,
    val nameMethodPayment: String = "",
    val temp1: String = "",
    val temp2: String = "",
    val orderCode: String = "",
    val titleDropProductSuccess: String = "",
    val imageBitmap: ImageBitmap? = null,
    val voucherCode: String = "",
    val countDownPaymentByCash: Long = 0,
    val countDownPaymentByOnline: Long = 0,
    val numberCashNeedReturn: Int = 0,
    val initSetup: InitSetup? = null,
    val slotAtBottom: Slot? = null,
    val listAds: ArrayList<String> = arrayListOf(),
    val listBigAds: ArrayList<String> = arrayListOf(),
    val listSlot: ArrayList<Slot> = arrayListOf(),
    val listSlotInCard: ArrayList<Slot> = arrayListOf(),
    val listSlotInHome: ArrayList<Slot> = arrayListOf(),
    val listPaymentMethod: ArrayList<PaymentMethodResponse> = arrayListOf(),
    val promotion: PromotionResponse? = null,
    val totalAmount: Int = 0,
    val logSyncOrder: LogSyncOrder? = null,
)