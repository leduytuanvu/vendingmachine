package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogError(
    val machine_code: String,
    val error_type: String,
    val error_content: String,
    val event_time: String,
)