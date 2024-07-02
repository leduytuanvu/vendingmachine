package com.combros.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class GetQrCodeResponse(
    @SerializedName("qrCodeUrl") var qrCodeUrl: String?,
    @SerializedName("Payment_Provider_id") var paymentProviderId: String?,
    @SerializedName("Order_code") var orderCode: String?,
    @SerializedName("Payment_Refcode") var paymentRefcode: String?,
)