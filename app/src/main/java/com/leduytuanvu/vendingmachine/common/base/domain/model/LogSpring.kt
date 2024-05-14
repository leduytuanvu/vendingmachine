package com.leduytuanvu.vendingmachine.common.base.domain.model

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class LogSpring(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("slot") val slot: Int,
    @SerializedName("number_of_revolutions") val numberOfRevolutions: Int,
)