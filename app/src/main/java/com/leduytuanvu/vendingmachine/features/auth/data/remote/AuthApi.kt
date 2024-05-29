package com.leduytuanvu.vendingmachine.features.auth.data.remote

import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.AccountResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {
    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/user-service/user/auth")
    suspend fun login(
        @Query("vend_code") vendCode: String,
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/machine-service/machine_employee/list")
    suspend fun getListAccount(@Query("vend_code") vendCode: String): BaseListResponse<AccountResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/machine-service/vm/active")
    suspend fun activateTheMachine(
        @Body activateTheMachineRequest: ActivateTheMachineRequest
    ): BaseResponse<String>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/machine-service/vm/deactivate")
    suspend fun deactivateTheMachine(
        @Body deactivateTheMachineRequest: ActivateTheMachineRequest
    ): BaseResponse<String>
}