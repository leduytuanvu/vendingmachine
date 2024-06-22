package com.leduytuanvu.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class InformationOfMachineResponse (
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DataInformationMachineResponse
)

data class DataInformationMachineResponse(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("commany_name") val companyName: String,
    @SerializedName("hotline") val hotline: String,
    @SerializedName("description") val description: String,
    @SerializedName("android_id") val androidId: String,
    @SerializedName("status") val status: Int,
)