package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogError(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("error_type") val errorType: String,
    @SerializedName("error_content") val errorContent: String,
    @SerializedName("event_time") val eventTime: String,
)