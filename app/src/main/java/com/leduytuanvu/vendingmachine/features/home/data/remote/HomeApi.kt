package com.leduytuanvu.vendingmachine.features.home.data.remote

import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.AccountResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.PromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {
//    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
//    @POST("/payment-service/deposit_withdraw/create")
//    suspend fun depositAndWithdrawMoney(@Body depositAndWithdrawMoneyRequest: DepositAndWithdrawMoneyRequest): PaymentCashResponse

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/promotion/calculation")
    suspend fun getPromotion(@Body promotionRequest: PromotionRequest): BaseResponse<PromotionResponse>
}