package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogFill(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("fill_type") val fillType: String,
    @SerializedName("content") val content: String,
    @SerializedName("event_time") val eventTime: String,
)