package com.leduytuanvu.vendingmachine.features.settings.data.remote

import com.leduytuanvu.vendingmachine.features.settings.data.model.response.InformationOfMachineResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.LayoutResponse
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.LoadProductResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

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
}