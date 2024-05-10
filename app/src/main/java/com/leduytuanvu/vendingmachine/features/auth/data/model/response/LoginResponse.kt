package com.leduytuanvu.vendingmachine.features.auth.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val tokenType: String?,
    @SerializedName("access_token_expires") val accessTokenExpires: String?,
    @SerializedName("time_current") val timeCurrent: String?,
    @SerializedName("time_token_expires") val timeTokenExpires: String?,
)