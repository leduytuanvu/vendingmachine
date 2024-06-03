package com.leduytuanvu.vendingmachine.common.base.domain.model

data class LogUpdateDeliveryStatus (
    val machineCode: String,
    val androidId: String,
    val orderCode: String,
    val deliveryStatus: String,
    val isSent: Boolean,
)