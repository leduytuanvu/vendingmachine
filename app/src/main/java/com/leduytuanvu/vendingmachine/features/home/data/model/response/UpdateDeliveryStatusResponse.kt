package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class UpdateDeliveryStatusResponse(
    @SerializedName("machine_code") var machineCode: String?,
)