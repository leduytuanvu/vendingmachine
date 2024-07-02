package com.combros.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class UpdateInventoryResponse(
    @SerializedName("machine_code") val machineCode: String?,
    @SerializedName("cabinet_code") val cabinetCode: String?,
    @SerializedName("product_layout_id") val productLayoutId: String?,
    @SerializedName("slot") val slot: Int?,
    @SerializedName("remaining") val remaining: Int?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("id") val id: String?,
)