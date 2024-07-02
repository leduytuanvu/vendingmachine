package com.combros.vendingmachine.features.home.domain.model

import com.google.gson.annotations.SerializedName

data class Extra(
    @SerializedName("carts") val carts: ArrayList<CartExtra>,
    @SerializedName("total_amount") val totalAmount: Int,
    @SerializedName("total_discount") val totalDiscount: Int,
    @SerializedName("payment_amount") val paymentAmount: Int,
    @SerializedName("reward_value") val rewardValue: Int,
    @SerializedName("reward_max_value") val rewardMaxValue: String,
    @SerializedName("machine_code") val machineCode: String,
)

data class CartExtra(
    @SerializedName("product_code") val productCode: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("price") val price: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("discount") val discount: Int,
    @SerializedName("amount") val amount: Int,
)