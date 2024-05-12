package com.leduytuanvu.vendingmachine.features.splash.presentation.view_model

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.features.base.domain.model.LogAuthy
import com.leduytuanvu.vendingmachine.features.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.core.util.toLogError
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class SplashViewModel @Inject constructor (
    private val splashRepository: SplashRepository,
    private val authRepository: AuthRepository,
    private val localStorageDatasource: LocalStorageDatasource,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val context: Context,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashViewState())
    val state = _state.asStateFlow()

    fun checkFileInitSetupExists(navController: NavHostController) {
        viewModelScope.launch {
            var isFileInitSetupExists = false
            try {
                _state.update { it.copy(isLoading = true) }
                isFileInitSetupExists = splashRepository.isFileInitSetupExists()
                navController.popBackStack()
                if(isFileInitSetupExists) {
                    val initSetup: InitSetup = splashRepository.getInitSetupFromLocal()
                    if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox!!) == -1) {
                        logger.info("Open port cash box is error")
                    } else {
                        logger.info("Open port cash box is success")
                        portConnectionDataSource.startReadingCashBox()
                    }
                    if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine!!) == -1) {
                        logger.info("Open port vending machine is error")
                    } else {
                        logger.info("Open port vending machine is success")
                        portConnectionDataSource.startReadingVendingMachine()
                    }
                    navController.navigate(Screens.HomeScreenRoute.route)
                } else {
                    navController.navigate(Screens.InitSettingScreenRoute.route)
                }
            } catch (e: Exception) {
                if(isFileInitSetupExists) {
                    val initSetup: InitSetup = splashRepository.getInitSetupFromLocal()
                    val logError = LogError(
                        machine_code = initSetup.vendCode,
                        error_type = "application",
                        error_content = e.message ?: "unknown error",
                        event_time = LocalDateTime.now().toDateTimeString(),
                    )
                    logger.addNewLogToLocalLogServerLocal(
                        eventType = "error",
                        severity = "normal",
                        eventData = logError,
                        localStorageDatasource = localStorageDatasource,
                    )
                }
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun writeInitSetupToLocal(
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

                    val logAuthy = LogAuthy(
                        machine_code = inputVendingMachineCode,
                        authy_type = "login",
                        username = loginRequest.username,
                        event_time = "",
                    )

                    logger.addNewLogToLocalLogServerLocal(
                        eventType = "authy",
                        severity = "normal",
                        eventData = logAuthy,
                        localStorageDatasource = localStorageDatasource,
                    )

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
                    val androidId = splashRepository.getAndroidId(context = context)
                    val initSetup = InitSetup(
                        vendCode = inputVendingMachineCode,
                        androidId = androidId,
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
                        localStorageDatasource.writeData(pathFileInitSetup, localStorageDatasource.gson.toJson(initSetup))
                        if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox!!) == -1) {
                            logger.info("Open port cash box is error")
                        } else {
                            logger.info("Open port cash box is success")
                            portConnectionDataSource.startReadingCashBox()
                        }
                        if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine!!) == -1) {
                            logger.info("Open port vending machine is error")
                        } else {
                            logger.info("Open port vending machine is success")
                            portConnectionDataSource.startReadingVendingMachine()
                        }
                        navController.navigate(Screens.SettingScreenRoute.route)
                        sendEvent(Event.Toast("Setup init success"))
                    } else {
                        sendEvent(Event.Toast("Authentication failed!"))
                    }
                }
            } catch (e: Exception) {
                logger.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = e.toLogError(localStorageDatasource)
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}