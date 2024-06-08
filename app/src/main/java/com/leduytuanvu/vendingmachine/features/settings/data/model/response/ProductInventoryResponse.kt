package com.leduytuanvu.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class ProductInventoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("cabinet_code") val cabinetCode: String,
    @SerializedName("product_layout_id") val productLayoutId: Int,
    @SerializedName("slot") val slot: Int?,
    @SerializedName("remaining") val remaining: Int?,
    @SerializedName("is_active") val isActive: Int?,
)