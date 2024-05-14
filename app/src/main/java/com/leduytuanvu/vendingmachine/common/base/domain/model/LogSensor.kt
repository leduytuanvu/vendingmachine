package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogSensor(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("cabinet_code") val cabinetCode: String,
    @SerializedName("product_code") val productCode: String,
    @SerializedName("slot") val slot: String,
    @SerializedName("status") val status: String,
    @SerializedName("event_time") val eventTime: String,
)