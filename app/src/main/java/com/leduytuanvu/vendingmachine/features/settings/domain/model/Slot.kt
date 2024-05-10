package com.leduytuanvu.vendingmachine.features.settings.domain.model

data class Slot(
    var slot: Int,
    var productCode: String,
    var productName: String,
    var inventory: Int,
    var capacity: Int,
    var price: Int,
    var isCombine: String,
    var springType: String,
    var status: Int,
    var slotCombine: Int,
    var isLock: Boolean
)