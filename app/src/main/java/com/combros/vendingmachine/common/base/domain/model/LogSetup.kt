package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogSetup(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("operation_content") val operationContent: String,
    @SerializedName("operation_type") val operationType: String,
    @SerializedName("username") val username: String,
    @SerializedName("event_time") val eventTime: String,
)