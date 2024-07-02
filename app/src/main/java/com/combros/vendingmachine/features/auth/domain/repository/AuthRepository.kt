package com.combros.vendingmachine.features.auth.domain.repository

import com.combros.vendingmachine.common.base.data.model.BaseListResponse
import com.combros.vendingmachine.common.base.data.model.BaseResponse
import com.combros.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.combros.vendingmachine.features.auth.data.model.request.LoginRequest
import com.combros.vendingmachine.features.auth.data.model.response.AccountResponse
import com.combros.vendingmachine.features.auth.data.model.response.LoginResponse

interface AuthRepository {
    suspend fun login(vendCode: String, loginRequest: LoginRequest) : LoginResponse
    suspend fun activateTheMachine(activateTheMachineRequest: ActivateTheMachineRequest) : BaseResponse<String>
    suspend fun deactivateTheMachine(deactivateTheMachineRequest: ActivateTheMachineRequest) : BaseResponse<String>
    suspend fun decodePassword(password: String) : String
    suspend fun encodePassword(decodeString: String) : String
    suspend fun getListAccount(vendCode: String) : BaseListResponse<AccountResponse>
}