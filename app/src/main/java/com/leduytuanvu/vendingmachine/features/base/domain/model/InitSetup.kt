package com.leduytuanvu.vendingmachine.features.base.domain.model

data class InitSetup (
    var vendCode: String,
    var androidId: String,
    var username: String?,
    var password: String?,
    var portVendingMachine: String?,
    var baudRateVendingMachine: String?,
    var portCashBox: String?,
    var baudRateCashBox: String?,
    var typeVendingMachine: String?,
    var role: String?,
)