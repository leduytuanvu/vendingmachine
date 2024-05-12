package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogFill(
    val machine_code: String,
    val fill_type: String,
    val content: String,
    val event_time: String,
)