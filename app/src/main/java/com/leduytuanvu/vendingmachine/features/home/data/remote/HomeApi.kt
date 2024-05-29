package com.leduytuanvu.vendingmachine.features.home.data.remote

import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.AccountResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.request.CheckPaymentResultOnlineRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.GetQrCodeRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.LogServerRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.PromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.request.UpdatePromotionRequest
import com.leduytuanvu.vendingmachine.features.home.data.model.response.CheckPaymentResultOnlineResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.DepositAndWithdrawMoneyResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.GetQrCodeResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.LogServerResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.PromotionResponse
import com.leduytuanvu.vendingmachine.features.home.data.model.response.UpdatePromotionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface HomeApi {
    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/deposit_withdraw/create")
    suspend fun depositAndWithdrawMoney(@Body depositAndWithdrawMoneyRequest: DepositAndWithdrawMoneyRequest): BaseResponse<DepositAndWithdrawMoneyResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/promotion/calculation")
    suspend fun getPromotion(@Body promotionRequest: PromotionRequest): BaseResponse<PromotionResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("log-service/event_logs/multi")
    suspend fun logMulti(@Body request: LogServerRequest): BaseListResponse<LogServerResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/payment/create")
    suspend fun getQrCode(@Body getQrCodeRequest: GetQrCodeRequest): BaseResponse<GetQrCodeResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/voucher-service/voucher_history/create")
    suspend fun updatePromotion(@Body updatePromotion: UpdatePromotionRequest): BaseResponse<UpdatePromotionResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/payment/query")
    suspend fun checkResultPaymentOnline(@Body checkResultPaymentOnlineRequest: CheckPaymentResultOnlineRequest): BaseResponse<CheckPaymentResultOnlineResponse>
}