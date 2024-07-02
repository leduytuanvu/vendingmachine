package com.combros.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName
import com.combros.vendingmachine.features.settings.domain.model.Product

data class LoadProductResponse (
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ArrayList<Product>
)