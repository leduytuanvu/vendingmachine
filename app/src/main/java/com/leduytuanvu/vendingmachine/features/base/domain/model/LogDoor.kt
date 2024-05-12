package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogDoor(
    val machine_code: String,
    val cabinet_code: String,
    val operation_type: String,
    val event_time: String,
)