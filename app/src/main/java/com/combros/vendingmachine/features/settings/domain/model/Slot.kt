package com.combros.vendingmachine.features.settings.domain.model

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
    var isLock: Boolean,
    var isEnable: Boolean,
    var messDrop: String,
) {
    constructor(
        productCode: String,
        productName: String,
        inventory: Int,
        price: Int,
    ) : this(
        slot = 0,
        productCode = productCode,
        productName = productName,
        inventory = inventory,
        capacity = 0,
        price = price,
        isCombine = "no",
        springType = "lo xo don",
        status = 1,
        slotCombine = 0,
        isLock = false,
        isEnable = true,
        messDrop = "",
    )
}