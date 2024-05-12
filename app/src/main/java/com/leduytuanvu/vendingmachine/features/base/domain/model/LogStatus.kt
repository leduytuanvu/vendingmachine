package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogStatus(
    val machine_code: String,
    val network_type: String,
    val ip: String,
    val network_status: String,
    val power_info: String,
    val event_time: String,
)