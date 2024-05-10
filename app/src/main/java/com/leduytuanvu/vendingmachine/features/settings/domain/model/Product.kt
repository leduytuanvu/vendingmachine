package com.leduytuanvu.vendingmachine.features.settings.domain.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("brief") val brief: String?,
    @SerializedName("price") val price: Int?,
    @SerializedName("product_code") val productCode: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("spings_type") val spingsType: String?,
    @SerializedName("category") val category: Int?,
)