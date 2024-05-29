package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class PromotionResponse(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("carts") var carts: ArrayList<DataPromotionResponse>?,
    @SerializedName("total_amount") var totalAmount: Int?,
    @SerializedName("total_discount") var totalDiscount: Int?,
    @SerializedName("payment_amount") var paymentAmount: Int?,
    @SerializedName("reward_type") var rewardType: String?,
    @SerializedName("reward_value") var rewardValue: Int?,
    @SerializedName("reward_max_value") var rewardMaxValue: String?,
    @SerializedName("voucher_code") var voucherCode: String?,
    @SerializedName("campaign_id") var campaignId: String?,
    @SerializedName("promotion_id") var promotionId: String?,
)

data class DataPromotionResponse(
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("price") var price: Int?,
    @SerializedName("quantity") var quantity: Int?,
    @SerializedName("discount") var discount: Int?,
    @SerializedName("amount") var amount: Int?,
)