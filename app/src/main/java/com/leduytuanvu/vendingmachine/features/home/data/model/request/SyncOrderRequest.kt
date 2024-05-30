package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName

data class SyncOrderRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("order_time") var orderTime: String?,
    @SerializedName("total_amount") var totalAmount: Int?,
    @SerializedName("total_discount") var totalDiscount: Int?,
    @SerializedName("payment_amount") var paymentAmount: Int?,
    @SerializedName("payment_method_id") var paymentMethod_id: String?,
    @SerializedName("payment_time") var paymentTime: String?,
    @SerializedName("time_synchronized_to_server") var timeSynchronizedToServer: String?,
    @SerializedName("time_release_products") var timeReleaseProducts: String?,
    @SerializedName("reward_type") var rewardType: String?,
    @SerializedName("reward_value") var rewardValue: Int?,
    @SerializedName("reward_max_value") var rewardMaxValue: Int?,
    @SerializedName("payment_status") var paymentStatus: String?,
    @SerializedName("delivery_status") var deliveryStatus: String?,
    @SerializedName("voucher_code") var voucherCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("product_details") var productDetails: ArrayList<ListProductSyncOrder>,
)

data class ListProductSyncOrder(
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("price") var price: Int?,
    @SerializedName("quantity") var quantity: Int?,
    @SerializedName("discount") var discount: Int?,
    @SerializedName("amount") var amount: String?,
    @SerializedName("slot") var slot: String?,
    @SerializedName("cabinet_code") var cabinetCode: String?,
)