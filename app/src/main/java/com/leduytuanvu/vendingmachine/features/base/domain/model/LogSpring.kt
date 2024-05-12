package com.leduytuanvu.vendingmachine.features.base.domain.model

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class LogSpring(
    val machine_code: String,
    val slot: Int,
    val number_of_revolutions: Int,
)