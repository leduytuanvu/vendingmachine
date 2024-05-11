package com.leduytuanvu.vendingmachine.features.splash.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.models.InitSetup
import com.leduytuanvu.vendingmachine.core.storage.LocalStorage
import com.leduytuanvu.vendingmachine.core.util.Constants
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.exceptionHandling
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor (
    private val splashRepository: SplashRepository,
    private val authRepository: AuthRepository,
    private val localStorage: LocalStorage,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashViewState())
    val state = _state.asStateFlow()

    fun fileInitSetupExists(navController: NavHostController) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val isVendCodeExists = splashRepository.fileInitSetupExists()
                navController.popBackStack()
                if(isVendCodeExists) {
                    navController.navigate(Screens.SettingScreenRoute.route)
                } else {
                    navController.navigate(Screens.InitSettingScreenRoute.route)
                }
            } catch (e: Exception) {
                e.exceptionHandling(localStorage, exception = e, inFunction = "fileInitSetupExists()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveInitSetup(
        inputVendingMachineCode: String,
        portCashBox: String,
        portVendingMachine: String,
        typeVendingMachine: String,
        loginRequest: LoginRequest,
        navController: NavHostController,
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                if(inputVendingMachineCode.trim().isEmpty()) {
                    sendEvent(Event.Toast("Vending machine code must not empty"))
                } else if(portCashBox.isEmpty()) {
                    sendEvent(Event.Toast("Port cash box must not empty"))
                } else if(portVendingMachine.isEmpty()) {
                    sendEvent(Event.Toast("Port vending machine must not empty"))
                } else if(typeVendingMachine.isEmpty()) {
                    sendEvent(Event.Toast("Type vending machine must not empty"))
                } else if(loginRequest.username.isEmpty()) {
                    sendEvent(Event.Toast("Username must not empty"))
                } else if(loginRequest.password.isEmpty()) {
                    sendEvent(Event.Toast("Password must not empty"))
                } else {
                    val passwordEncode = authRepository.encodePassword(loginRequest.password)
                    val passwordDecode = authRepository.decodePassword(passwordEncode)
                    val response = authRepository.login(inputVendingMachineCode, loginRequest)
                    val initSetup = InitSetup(
                        vendCode = inputVendingMachineCode,
                        androidId = "",
                        username = loginRequest.username,
                        password = passwordDecode,
                        portVendingMachine = portVendingMachine,
                        portCashBox = portCashBox,
                        typeVendingMachine = typeVendingMachine,
                        role = ""
                    )
                    if(response.accessToken!!.isNotEmpty()) {
                        localStorage.writeData(localStorage.fileInitSetup, localStorage.gson.toJson(initSetup))
                        navController.navigate(Screens.SettingScreenRoute.route)
                        sendEvent(Event.Toast("Setup init success"))
                    } else {
                        sendEvent(Event.Toast("Authentication failed!"))
                    }
                }
            } catch (e: Exception) {
                e.exceptionHandling(localStorage, exception = e, inFunction = "saveInitSetup()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}