package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdatePromotionRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("extra") var extra: String?,
    @SerializedName("status") var status: Boolean?,
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("voucher_code") var voucherCode: String?,
    @SerializedName("promotion_id") var promotionId: String?,
    @SerializedName("campaign_id") var campaignId: String?,
)