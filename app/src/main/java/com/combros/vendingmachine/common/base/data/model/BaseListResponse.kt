package com.combros.vendingmachine.common.base.data.model

import com.google.gson.annotations.SerializedName

data class BaseListResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ArrayList<T>
)