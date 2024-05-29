package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogsLocal(
    @SerializedName("event_id") val eventId: String,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("event_time") val eventTime: String,
    @SerializedName("event_data") val eventData: String,
    @SerializedName("isSent") var isSent: Boolean,
)