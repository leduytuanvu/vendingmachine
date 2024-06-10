package com.leduytuanvu.vendingmachine.common.base.domain.model

import com.leduytuanvu.vendingmachine.features.home.data.model.request.ItemProductInventoryRequest

class LogUpdateInventory (
    val machineCode: String,
    val androidId: String,
    val productList: ArrayList<ItemProductInventoryRequest>,
    var isSent: Boolean,
)