package com.combros.vendingmachine.common.base.domain.model

import com.combros.vendingmachine.features.home.data.model.request.ProductSyncOrderRequest

data class LogSyncOrder(
    var machineCode: String?,
    var androidId: String?,
    var orderCode: String?,
    var orderTime: String?,
    var totalAmount: Int?,
    var totalDiscount: Int?,
    var paymentAmount: Int?,
    var paymentMethodId: String?,
    var paymentTime: String?,
    var timeSynchronizedToServer: String?,
    var timeReleaseProducts: String?,
    var rewardType: String?,
    var rewardValue: String?,
    var rewardMaxValue: Int?,
    var paymentStatus: String?,
    var deliveryStatus: String?,
    var voucherCode: String?,
    var productDetails: ArrayList<ProductSyncOrderRequest>,
    var isSent: Boolean,
)