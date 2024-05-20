package com.leduytuanvu.vendingmachine.features.auth.data.model.response

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("expired_time") val expiredTime: String?,
    @SerializedName("account_status") val accountStatus: Int?,
    @SerializedName("machine_code") val machineCode: String?,
    @SerializedName("username") val username: String?,
)