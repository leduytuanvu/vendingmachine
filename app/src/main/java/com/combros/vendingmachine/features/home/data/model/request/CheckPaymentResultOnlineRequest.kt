package com.combros.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class CheckPaymentResultOnlineRequest(
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("order_code") val orderCode: String,
    @SerializedName("store_id") val storeId: String,
)