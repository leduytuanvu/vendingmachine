package com.combros.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class LogServerResponse(
    @SerializedName("id") var id: String?,
    @SerializedName("machine_code") var machine_code: String?,
    @SerializedName("android_id") var android_id: String?,
    @SerializedName("event_type") var event_type: String?,
    @SerializedName("event_time") var event_time: String?,
    @SerializedName("event_data") var event_data: String?,
)