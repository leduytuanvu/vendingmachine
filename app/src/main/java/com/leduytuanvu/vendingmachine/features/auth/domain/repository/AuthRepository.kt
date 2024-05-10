package com.leduytuanvu.vendingmachine.features.auth.domain.repository

import android.util.Base64
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.response.LoginResponse

interface AuthRepository {
    suspend fun login(vendCode: String, loginRequest: LoginRequest) : LoginResponse
    suspend fun decodePassword(password: String) : String
    suspend fun encodePassword(decodeString: String) : String
}