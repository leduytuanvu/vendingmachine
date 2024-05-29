package com.leduytuanvu.vendingmachine.features.auth.data.model.request

import com.google.gson.annotations.SerializedName

class ActivateTheMachineRequest (
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("android_id") val androidId: String,
)
