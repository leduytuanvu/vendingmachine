package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogStatus(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("network_type") val networkType: String,
    @SerializedName("ip") val ip: String,
    @SerializedName("network_status") val networkStatus: String,
    @SerializedName("power_info") val powerInfo: String,
    @SerializedName("event_time") val eventTime: String,
)