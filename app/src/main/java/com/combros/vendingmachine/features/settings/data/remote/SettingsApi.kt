package com.combros.vendingmachine.features.settings.data.remote

import com.combros.vendingmachine.common.base.data.model.BaseListResponse
import com.combros.vendingmachine.common.base.data.model.BaseResponse
import com.combros.vendingmachine.features.settings.data.model.request.EndOfSessionRequest
import com.combros.vendingmachine.features.settings.data.model.response.DataPaymentMethodResponse
import com.combros.vendingmachine.features.settings.data.model.response.ImageResponse
import com.combros.vendingmachine.features.settings.data.model.response.InformationOfMachineResponse
import com.combros.vendingmachine.features.settings.data.model.response.LayoutResponse
import com.combros.vendingmachine.features.settings.data.model.response.LoadProductResponse
import com.combros.vendingmachine.features.settings.data.model.response.PriceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SettingsApi {
    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/product-service/product_to_machine/list_by_machine/{vend_code}")
    suspend fun getListProduct(@Path("vend_code") vendCode: String): LoadProductResponse

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/product-service/product_layout/list_by_machine/{vend_code}")
    suspend fun getLayout(@Path("vend_code") vendCode: String): LayoutResponse

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/machine-service/vm/detail/{vend_code}")
    suspend fun getInformationOfMachine(@Path("vend_code") vendCode: String): InformationOfMachineResponse

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/machine-service/setting/list")
    suspend fun getListPaymentMethod(
        @Query("vend_code") vendCode: String,
        @Query("setting_type") settingType: String
    ): BaseListResponse<DataPaymentMethodResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/product-service/product_to_machine/price_by_machine/{vend_code}")
    suspend fun getListPriceOfProduct(@Path("vend_code") vendCode: String): BaseListResponse<PriceResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @GET("/product-service/product_to_machine/image_by_machine/{vend_code}")
    suspend fun getListImageOfProduct(@Path("vend_code") vendCode: String): BaseListResponse<ImageResponse>

    @Headers("Content-Type: application/json", "Accept-Language: en-US")
    @POST("/machine-service/vm/end_of_session")
    suspend fun endOfSession(@Body endOfSessionRequest: EndOfSessionRequest): BaseResponse<String>
}