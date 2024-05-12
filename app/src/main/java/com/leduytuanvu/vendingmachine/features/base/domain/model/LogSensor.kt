package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogSensor(
    val machine_code: String,
    val cabinet_code: String,
    val product_code: String,
    val slot: String,
    val status: String,
    val event_time: String,
)