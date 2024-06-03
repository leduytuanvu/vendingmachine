package com.leduytuanvu.vendingmachine.features.auth.domain.repository

import android.util.Base64
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseListResponse
import com.leduytuanvu.vendingmachine.common.base.data.model.BaseResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.AccountResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse

interface AuthRepository {
    suspend fun login(vendCode: String, loginRequest: LoginRequest) : LoginResponse
    suspend fun activateTheMachine(activateTheMachineRequest: ActivateTheMachineRequest) : BaseResponse<String>
    suspend fun deactivateTheMachine(deactivateTheMachineRequest: ActivateTheMachineRequest) : BaseResponse<String>
    suspend fun decodePassword(password: String) : String
    suspend fun encodePassword(decodeString: String) : String
    suspend fun getListAccount(vendCode: String) : BaseListResponse<AccountResponse>
}