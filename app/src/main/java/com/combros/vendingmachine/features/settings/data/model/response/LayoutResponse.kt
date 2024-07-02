package com.combros.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class LayoutResponse (
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ArrayList<DataResponse>
)

data class DataResponse (
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("slot") val slot: SlotResponse,
)

data class SlotResponse (
    @SerializedName("slot") val slot: Int,
    @SerializedName("product_code") val productCode: String,
    @SerializedName("capicity") val capacity: Int,
    @SerializedName("is_combine") val isCombine: String,
    @SerializedName("spring_type") val springType: String,
    @SerializedName("status") val status: Int,
    @SerializedName("slot_combine") val slotCombine: Int,
)