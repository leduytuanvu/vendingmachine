package com.combros.vendingmachine.features.auth.data.repository

import android.util.Base64
import com.combros.vendingmachine.common.base.data.model.BaseListResponse
import com.combros.vendingmachine.common.base.data.model.BaseResponse
import com.combros.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.combros.vendingmachine.features.auth.data.remote.AuthApi
import com.combros.vendingmachine.features.auth.data.model.request.LoginRequest
import com.combros.vendingmachine.features.auth.data.model.response.AccountResponse
import com.combros.vendingmachine.features.auth.data.model.response.LoginResponse
import com.combros.vendingmachine.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val localStorageDatasource: LocalStorageDatasource,
) : AuthRepository {

    override suspend fun login(vendCode: String, loginRequest: LoginRequest) : LoginResponse {
        try {
            return authApi.login(vendCode, loginRequest)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun activateTheMachine(activateTheMachineRequest: ActivateTheMachineRequest): BaseResponse<String> {
        try {
            return authApi.activateTheMachine(activateTheMachineRequest)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deactivateTheMachine(deactivateTheMachineRequest: ActivateTheMachineRequest): BaseResponse<String> {
        try {
            return authApi.deactivateTheMachine(deactivateTheMachineRequest)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun decodePassword(password: String): String {
        try {
            val data = Base64.decode(password, Base64.DEFAULT)
            return String(data, Charsets.UTF_8)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun encodePassword(decodeString: String): String {
        try {
//            Logger.debug("encodePassword: $decodeString")
            val tmpString = decodeString + "567890VENDINGMACHINE"
            val data = tmpString.toByteArray(Charsets.UTF_8)
            return Base64.encodeToString(data, Base64.DEFAULT)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListAccount(vendCode: String): BaseListResponse<AccountResponse> {
        try {
            val response = authApi.getListAccount(vendCode)
            return response
        } catch (e: Exception) {
            throw e
        }
    }
}