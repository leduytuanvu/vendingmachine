package com.leduytuanvu.vendingmachine.common.base.domain.model

data class LogUpdateDeliveryStatus (
    val orderCode: String,
    val deliveryStatus: String,
    val machineCode: String,
    val productCode: String,
    val androidId: String,
    var isSent: Boolean,
)