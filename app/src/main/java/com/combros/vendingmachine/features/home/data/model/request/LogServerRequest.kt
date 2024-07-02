package com.combros.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName
import com.combros.vendingmachine.common.base.domain.model.LogServer

data class LogServerRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("events") var events: ArrayList<LogServer>?,
)