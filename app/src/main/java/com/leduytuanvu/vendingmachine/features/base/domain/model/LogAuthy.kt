package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogAuthy(
    val machine_code: String,
    val authy_type: String,
    val username: String,
    val event_time: String,
)