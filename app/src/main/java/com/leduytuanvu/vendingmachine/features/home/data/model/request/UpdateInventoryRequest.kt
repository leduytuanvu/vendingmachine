package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateInventoryRequest(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("android_id") val androidId: String,
    @SerializedName("product_list") val productList: ArrayList<ItemProductInventoryRequest>,
)

data class ItemProductInventoryRequest(
    @SerializedName("cabinet_code") val cabinetCode: String?,
    @SerializedName("product_layout_id") val productLayoutId: String?,
    @SerializedName("slot") val slot: Int?,
    @SerializedName("remaining") val remaining: Int?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("id") val id: String?,
)