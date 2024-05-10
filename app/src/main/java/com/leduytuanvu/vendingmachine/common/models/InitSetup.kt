package com.leduytuanvu.vendingmachine.common.models

data class InitSetup (
    val vendCode: String,
    val androidId: String,
    val username: String?,
    val password: String?,
    val portVendingMachine: String?,
    val portCashBox: String?,
    val typeVendingMachine: String?,
    val role: String?,
)