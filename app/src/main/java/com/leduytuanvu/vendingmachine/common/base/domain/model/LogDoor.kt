package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogDoor(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("cabinet_code") val cabinetCode: String,
    @SerializedName("operation_type") val operationType: String,
    @SerializedName("event_time") val eventTime: String,
)