package com.leduytuanvu.vendingmachine.features.auth.data.repository

import android.util.Base64
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.features.auth.data.remote.AuthApi
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.AccountResponse
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
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
            val tmpString = decodeString + "_leduytuanvu"
            val data = tmpString.toByteArray(Charsets.UTF_8)
            return Base64.encodeToString(data, Base64.DEFAULT)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListAccount(decodeString: String): ArrayList<AccountResponse> {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(pathFileInitSetup) ?: return arrayListOf()
            val response = authApi.getListAccount(initSetup.vendCode)
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }
}