package com.combros.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class PriceResponse (
    @SerializedName("product_code") val productCode: String?,
    @SerializedName("price") val price: Int?,
)