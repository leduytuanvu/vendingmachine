package com.leduytuanvu.vendingmachine.common.base.domain.model

data class InitSetup (
    var vendCode: String,
    var androidId: String,
    var username: String,
    var password: String,
    var portVendingMachine: String,
    var baudRateVendingMachine: String,
    var portCashBox: String,
    var baudRateCashBox: String,
    var typeVendingMachine: String,
    var fullScreenAds: String,
    var withdrawalAllowed: String,
    var autoStartApplication: String,
    var layoutHomeScreen: String,
    var timeTurnOnLight: String,
    var timeTurnOffLight: String,
    var dropSensor: String,
    var inchingMode: String,
    var timeToJumpToAdsScreen: String,
    var glassHeatingMode: String,
    var highestTempWarning: String,
    var lowestTempWarning: String,
    var temperature: String,
    var timeoutPayment: String,
    var role: String,
) {
    override fun toString(): String {
        return "vendCode='$vendCode', androidId='$androidId', username='$username', password='$password', portVendingMachine='$portVendingMachine', baudRateVendingMachine='$baudRateVendingMachine', portCashBox='$portCashBox', baudRateCashBox='$baudRateCashBox', typeVendingMachine='$typeVendingMachine', role='$role'"
    }
}