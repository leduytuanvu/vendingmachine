package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

//data class LogDepositWithdraw(
//    @SerializedName("vend_code") val vendCode: String,
//    @SerializedName("transaction_type") val transactionType: String,
//    @SerializedName("denomination_type") val denominationType: Int,
//    @SerializedName("quantity") val quantity: Int,
//    @SerializedName("current_balance") val currentBalance: Int,
//    @SerializedName("syn_time") val synTime: String,
//    @SerializedName("isSent") var isSent: Boolean,
//)
data class LogDepositWithdraw(
    @SerializedName("vend_code") val vendCode: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("denomination_type") val denominationType: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("current_balance") val currentBalance: Int,
    @SerializedName("syn_time") val synTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("isSent") var isSent: Boolean,
)