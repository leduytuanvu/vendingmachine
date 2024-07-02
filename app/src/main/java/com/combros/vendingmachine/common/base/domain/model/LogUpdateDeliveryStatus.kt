package com.combros.vendingmachine.common.base.domain.model

data class LogUpdateDeliveryStatus (
    val orderCode: String,
    val deliveryStatus: String,
    val deliveryStatusNote: String,
    val machineCode: String,
    val productCode: String,
    val androidId: String,
    val slot: Int,
    var isSent: Boolean,
)