package com.leduytuanvu.vendingmachine.features.base.domain.model

data class LogServerLocal(
    val event_id: String,
    val event_type: String,
    val severity: String,
    val event_time: String,
    val event_data: String,
    val isSent: Boolean,
)