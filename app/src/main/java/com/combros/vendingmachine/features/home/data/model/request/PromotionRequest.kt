package com.combros.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class PromotionRequest (
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("voucher_code") var voucherCode: String?,
    @SerializedName("total_amount") var totalAmount: Int?,
    @SerializedName("total_discount") var totalDiscount: Int?,
    @SerializedName("payment_amount") var paymentAmount: Int?,
    @SerializedName("carts") var carts: ArrayList<ItemPromotionRequest>?,
)

data class ItemPromotionRequest (
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("price") var price: Int?,
    @SerializedName("quantity") var quantity: Int?,
    @SerializedName("discount") var discount: Int?,
    @SerializedName("amount") var amount: Int?,
    @SerializedName("slot") var slot: Int?,
)