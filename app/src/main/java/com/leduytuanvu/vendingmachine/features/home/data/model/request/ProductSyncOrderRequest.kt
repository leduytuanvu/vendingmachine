package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class ProductSyncOrderRequest(
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("price") var price: String?,
    @SerializedName("quantity") var quantity: Int?,
    @SerializedName("discount") var discount: Int?,
    @SerializedName("amount") var amount: String?,
    @SerializedName("delivery_status") var deliveryStatus: String?,
    @SerializedName("slot") var slot: Int?,
    @SerializedName("cabinet_code") var cabinetCode: String?,
)