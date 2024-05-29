package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServer

data class LogServerRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("events") var events: ArrayList<LogServer>?,
)