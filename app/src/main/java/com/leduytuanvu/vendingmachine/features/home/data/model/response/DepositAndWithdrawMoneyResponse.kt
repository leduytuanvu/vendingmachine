package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class DepositAndWithdrawMoneyResponse(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("android_id") val androidId: String,
    @SerializedName("transaction_type") val transactionType: String,
    @SerializedName("denomination_type") val denominationType: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("current_balance") val currentBalance: Int,
    @SerializedName("syn_time") val synTime: String,
    @SerializedName("create_date") val createDate: String,
    @SerializedName("sync_data") val syncData: Int,
    @SerializedName("sync_pg") val syncPg: Int,
)