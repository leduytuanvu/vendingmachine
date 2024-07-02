package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogTemperature(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("cabinet_code") val cabinetCode: String,
    @SerializedName("current_temperature") val currentTemperature: String,
    @SerializedName("event_time") val eventTime: String,
)