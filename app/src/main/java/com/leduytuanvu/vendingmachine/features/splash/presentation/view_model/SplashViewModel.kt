package com.leduytuanvu.vendingmachine.features.splash.presentation.view_model

import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.models.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.exceptionHandling
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor (
    private val splashRepository: SplashRepository,
    private val authRepository: AuthRepository,
    private val localStorageDatasource: LocalStorageDatasource,
    private val portConnectionDataSource: PortConnectionDatasource,
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
                    val initSetup: InitSetup = localStorageDatasource.getDataFromPath(localStorageDatasource.fileInitSetup)!!
                    if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox!!) == -1) {
                        Logger.info("Open port cash box is error")
                        sendEvent(Event.Toast("OPEN PORT CASH BOX IS ERROR!"))
                    } else {
                        Logger.info("Open port cash box is success")
                        portConnectionDataSource.startReadingCashBox()
                    }
                    if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine!!) == -1) {
                        Logger.info("Open port vending machine is error")
                        sendEvent(Event.Toast("OPEN PORT VENDING MACHINE IS ERROR!"))
                    } else {
                        Logger.info("Open port vending machine is success")
                        portConnectionDataSource.startReadingVendingMachine()
                    }
                    navController.navigate(Screens.SettingScreenRoute.route)
                } else {
                    navController.navigate(Screens.InitSettingScreenRoute.route)
                }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "fileInitSetupExists()")
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
                    val baudRateCashBox = "9600"
                    var baudRateVendingMachine = ""
                    baudRateVendingMachine = when (typeVendingMachine) {
                        "TCN" -> {
                            "9600"
                        }
                        "XY" -> {
                            "9600"
                        }
                        else -> {
                            "9600"
                        }
                    }
                    val initSetup = InitSetup(
                        vendCode = inputVendingMachineCode,
                        androidId = "",
                        username = loginRequest.username,
                        password = passwordDecode,
                        portVendingMachine = portVendingMachine,
                        baudRateVendingMachine = baudRateVendingMachine,
                        portCashBox = portCashBox,
                        baudRateCashBox = baudRateCashBox,
                        typeVendingMachine = typeVendingMachine,
                        role = ""
                    )
                    if(response.accessToken!!.isNotEmpty()) {
                        localStorageDatasource.writeData(localStorageDatasource.fileInitSetup, localStorageDatasource.gson.toJson(initSetup))
                        if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox!!) == -1) {
                            Logger.info("Open port cash box is error")
                            sendEvent(Event.Toast("OPEN PORT CASH BOX IS ERROR!"))
                        } else {
                            Logger.info("Open port cash box is success")
                            portConnectionDataSource.startReadingCashBox()
                        }
                        if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine!!) == -1) {
                            Logger.info("Open port vending machine is error")
                            sendEvent(Event.Toast("OPEN PORT VENDING MACHINE IS ERROR!"))
                        } else {
                            Logger.info("Open port vending machine is success")
                            portConnectionDataSource.startReadingVendingMachine()
                        }
                        navController.navigate(Screens.SettingScreenRoute.route)
                        sendEvent(Event.Toast("Setup init success"))
                    } else {
                        sendEvent(Event.Toast("Authentication failed!"))
                    }
                }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "saveInitSetup()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}