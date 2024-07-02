package com.combros.vendingmachine.features.settings.data.model.response

import com.google.gson.annotations.SerializedName

data class DataPaymentMethodResponse(
    @SerializedName("id") var id: String?,
    @SerializedName("machine_code") var machineCode: String?,
    @SerializedName("setting_type") var settingType: String?,
    @SerializedName("setting_data") var settingData: ArrayList<PaymentMethodResponse>?,
)

data class PaymentMethodResponse(
    @SerializedName("method_name") var methodName: String?,
    @SerializedName("brief") var brief: String?,
    @SerializedName("is_must_online") var isMustOnline: String?,
    @SerializedName("id") var id: Int?,
    @SerializedName("status") var status: Int?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("store_id") var storeId: String?,
)