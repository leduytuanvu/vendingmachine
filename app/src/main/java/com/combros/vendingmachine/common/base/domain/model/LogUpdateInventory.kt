package com.combros.vendingmachine.common.base.domain.model

import com.combros.vendingmachine.features.home.data.model.request.ItemProductInventoryRequest

class LogUpdateInventory (
    val machineCode: String,
    val androidId: String,
    val productList: ArrayList<ItemProductInventoryRequest>,
    var isSent: Boolean,
)