package com.leduytuanvu.vendingmachine.features.auth.data.remote

import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/user-service/user/auth")
    suspend fun login(
        @Query("vend_code") vendCode: String,
        @Body loginRequest: LoginRequest
    ): LoginResponse
}