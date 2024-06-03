package com.leduytuanvu.vendingmachine.features.home.data.model.response

import com.google.gson.annotations.SerializedName
import com.leduytuanvu.vendingmachine.features.home.data.model.request.ProductSyncOrderRequest

data class SyncOrderResponse(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("order_time") var orderTime: String?,
    @SerializedName("total_amount") var totalAmount: Int?,
    @SerializedName("total_discount") var totalDiscount: Int?,
    @SerializedName("payment_amount") var paymentAmount: Int?,
    @SerializedName("payment_method_id") var paymentMethodId: String?,
    @SerializedName("payment_time") var paymentTime: String?,
    @SerializedName("time_synchronized_to_server") var timeSynchronizedToServer: String?,
    @SerializedName("time_release_products") var timeReleaseProducts: String?,
    @SerializedName("reward_type") var rewardType: String?,
    @SerializedName("reward_value") var rewardValue: String?,
    @SerializedName("reward_max_value") var rewardMaxValue: Int?,
    @SerializedName("payment_status") var paymentStatus: String?,
    @SerializedName("delivery_status") var deliveryStatus: String?,
    @SerializedName("voucher_code") var voucherCode: String?,
    @SerializedName("product_details") var productDetails: ArrayList<ProductSyncOrderRequest>,
)