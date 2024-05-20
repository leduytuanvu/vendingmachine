package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class DepositAndWithdrawMoneyRequest(
    @SerializedName("vend_code") val vendCode: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("denomination_type") val denominationType: String,
    @SerializedName("quantity") val quantity: String,
    @SerializedName("current_balance") val currentBalance: String,
    @SerializedName("syn_time") val synTime: String,
)