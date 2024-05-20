package com.leduytuanvu.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product

data class PriceResponse (
    @SerializedName("product_code") val productCode: String?,
    @SerializedName("price") val price: Int?,
)