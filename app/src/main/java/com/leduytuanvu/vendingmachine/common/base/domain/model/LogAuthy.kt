package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogAuthy(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("authy_type") val authyType: String,
    @SerializedName("username") val username: String,
    @SerializedName("event_time") val eventTime: String,
)