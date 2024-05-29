package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName

data class UpdatePromotionResponse(
    @SerializedName("campaign_id") var campaignId: String?,
    @SerializedName("promotion_id") var promotionId: String?,
    @SerializedName("voucher_code") var voucherCode: Boolean?,
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("vend_code") var vendCode: String?,
    @SerializedName("status") var status: String?,
    @SerializedName("extra") var extra: String?,
    @SerializedName("date_add") var dateAdd: String?,
)