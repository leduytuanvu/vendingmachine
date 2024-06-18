package com.leduytuanvu.vendingmachine.features.settings.data.model.request

import com.google.gson.annotations.SerializedName

data class EndOfSessionRequest (
    @SerializedName("session_id") val sessionId: String?,
    @SerializedName("session_type") val sessionType: String?,
    @SerializedName("machine_code") val machineCode: String?,
    @SerializedName("android_id") val androidId: String?,
    @SerializedName("time_start") val timeStart: Long?,
    @SerializedName("time_end") val timeEnd: Long?,
    @SerializedName("money_data") val moneyData: ArrayList<MoneyDataRequest>?,
    @SerializedName("money_box") val moneyBox: ArrayList<MoneyBoxRequest>?,
)

data class MoneyDataRequest (
    @SerializedName("payment_method_id") val paymentMethodId: String?,
    @SerializedName("payment_amount") val paymentAmount: Int?,
    @SerializedName("order_quantity") val orderQuantity: Int?,
)

data class MoneyBoxRequest (
    @SerializedName("denomination") val denomination: Int?,
    @SerializedName("money_quantity") val moneyQuantity: Int?,
)