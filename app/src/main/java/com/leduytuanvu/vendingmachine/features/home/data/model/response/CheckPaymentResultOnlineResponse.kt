package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class CheckPaymentResultOnlineResponse(
    @SerializedName("return_code") val returnCode: Int,
    @SerializedName("return_message") val returnMessage: String,
    @SerializedName("sub_return_code") val subReturnCode: String,
    @SerializedName("sub_return_message") val subReturnMessage: String,
    @SerializedName("is_processing") val isProcessing: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("zp_trans_id") val zpTransId: String,
)