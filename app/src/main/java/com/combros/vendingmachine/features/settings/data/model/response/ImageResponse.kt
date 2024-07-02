package com.combros.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class ImageResponse (
    @SerializedName("product_code") val productCode: String?,
    @SerializedName("image_url") val imageUrl: String?,
)