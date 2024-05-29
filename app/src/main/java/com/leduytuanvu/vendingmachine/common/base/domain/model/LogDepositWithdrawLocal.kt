package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogDepositWithdrawLocal(
    @SerializedName("vend_code") val vendCode: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("denomination_type") val denominationType: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("current_balance") val currentBalance: Int,
    @SerializedName("syn_time") val synTime: String,
    @SerializedName("isSent") var isSent: Boolean,
)