package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateDeliveryStatusRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("delivery_status") var deliveryStatus: String?,
)