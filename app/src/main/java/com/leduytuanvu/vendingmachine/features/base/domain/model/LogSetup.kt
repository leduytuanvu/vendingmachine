package com.leduytuanvu.vendingmachine.features.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogSetup(
    val machine_code: String,
    val operation_content: String,
    val operation_type: String,
    val username: String,
    val event_time: String,
)