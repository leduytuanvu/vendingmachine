package com.combros.vendingmachine.features.home.data.remote

import com.combros.vendingmachine.common.base.data.model.BaseListResponse
import com.combros.vendingmachine.common.base.data.model.BaseResponse
import com.combros.vendingmachine.features.home.data.model.request.CheckPaymentResultOnlineRequest
import com.combros.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.combros.vendingmachine.features.home.data.model.request.GetQrCodeRequest
import com.combros.vendingmachine.features.home.data.model.request.LogServerRequest
import com.combros.vendingmachine.features.home.data.model.request.PromotionRequest
import com.combros.vendingmachine.features.home.data.model.request.SyncOrderRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdateDeliveryStatusRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdateInventoryRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdatePromotionRequest
import com.combros.vendingmachine.features.home.data.model.response.CheckPaymentResultOnlineResponse
import com.combros.vendingmachine.features.home.data.model.response.DepositAndWithdrawMoneyResponse
import com.combros.vendingmachine.features.home.data.model.response.GetQrCodeResponse
import com.combros.vendingmachine.features.home.data.model.response.LogServerResponse
import com.combros.vendingmachine.features.home.data.model.response.PromotionResponse
import com.combros.vendingmachine.features.home.data.model.response.SyncOrderResponse
import com.combros.vendingmachine.features.home.data.model.response.UpdateDeliveryStatusResponse
import com.combros.vendingmachine.features.home.data.model.response.UpdateInventoryResponse
import com.combros.vendingmachine.features.home.data.model.response.UpdatePromotionResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

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

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/payment/update_delivery_status")
    suspend fun updateDeliveryStatus(@Body updateDeliveryStatusRequest: UpdateDeliveryStatusRequest): BaseResponse<UpdateDeliveryStatusResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US") // Add headers annotation
    @POST("/payment-service/payment/synchronize")
    suspend fun syncOrder(@Body syncOrderRequest: SyncOrderRequest): BaseResponse<SyncOrderResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/product-service/product_inventory/update_multi")
    suspend fun updateMultiInventory(@Body request: UpdateInventoryRequest): BaseListResponse<UpdateInventoryResponse>
}