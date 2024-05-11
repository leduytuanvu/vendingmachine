package com.leduytuanvu.vendingmachine.common.models

data class InitSetup (
    val vendCode: String,
    val androidId: String,
    val username: String?,
    val password: String?,
    val portVendingMachine: String?,
    val baudRateVendingMachine: String?,
    val portCashBox: String?,
    val baudRateCashBox: String?,
    val typeVendingMachine: String?,
    val role: String?,
)