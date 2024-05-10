package com.leduytuanvu.vendingmachine.core.storage

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferencesStorage @Inject constructor(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("VendingMachineStorage", Context.MODE_PRIVATE)
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
    }

    fun saveAccessToken(accessToken: String) {
        sharedPreferences.edit().putString(ACCESS_TOKEN_KEY, accessToken).apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }
}