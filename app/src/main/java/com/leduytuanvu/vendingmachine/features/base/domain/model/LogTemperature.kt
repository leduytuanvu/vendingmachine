package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogTemperature(
    val machine_code: String,
    val cabinet_code: String,
    val current_temperature: String,
    val event_time: String,
)