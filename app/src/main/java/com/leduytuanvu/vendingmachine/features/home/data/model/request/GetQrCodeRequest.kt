package com.leduytuanvu.vendingmachine.features.home.data.model.request

import com.google.gson.annotations.SerializedName
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServer


data class GetQrCodeRequest(
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("android_id") var androidId: String?,
    @SerializedName("order_code") var orderCode: String?,
    @SerializedName("order_time") var orderTime: String?,
    @SerializedName("total_amount") var totalAmount: Int?,
    @SerializedName("total_discount") var totalDiscount: Int?,
    @SerializedName("payment_amount") var paymentAmount: Int?,
    @SerializedName("payment_method_id") var paymentMethodId: String?,
    @SerializedName("store_id") var storeId: String?,
    @SerializedName("product_details") var productDetails: ArrayList<ProductDetailRequest>?,
)

data class ProductDetailRequest(
    @SerializedName("product_code") var productCode: String?,
    @SerializedName("product_name") var productName: String?,
    @SerializedName("price") var price: Int?,
    @SerializedName("quantity") var quantity: Int?,
    @SerializedName("discount") var discount: Int?,
    @SerializedName("amount") var amount: Int?,
    @SerializedName("slot") var slot: Int?,
    @SerializedName("cabinet_code") var cabinetCode: String?,
)