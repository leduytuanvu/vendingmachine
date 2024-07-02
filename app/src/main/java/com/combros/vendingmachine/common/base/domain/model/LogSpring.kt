package com.combros.vendingmachine.common.base.domain.model

import com.google.gson.annotations.SerializedName

data class LogSpring(
    @SerializedName("machine_code") val machineCode: String,
    @SerializedName("slot") val slot: Int,
    @SerializedName("number_of_revolutions") val numberOfRevolutions: Int,
)