package com.combros.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateDeliveryStatusRequest(
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("delivery_status") var deliveryStatus: String?,
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("delivery_status_note") var deliveryStatusNote: String?,
    @SerializedName("slot") var slot: Int?,
)
