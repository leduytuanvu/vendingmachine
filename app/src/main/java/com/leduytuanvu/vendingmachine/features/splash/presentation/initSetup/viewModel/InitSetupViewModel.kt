package com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogAuthy
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.viewState.InitSetupViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class InitSetupViewModel @Inject constructor(
    private val baseRepository: BaseRepository,
    private val authRepository: AuthRepository,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(InitSetupViewState())
    val state = _state.asStateFlow()

    private fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    titleDialogWarning = mess,
                    isWarning = true,
                )
            }
        }
    }

    fun hideDialogWarning() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isWarning = false,
                    titleDialogWarning = "",
                )
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
        logger.info("writeInitSetupToLocal")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    if (inputVendingMachineCode.trim().isEmpty()) {
                        sendEvent(Event.Toast("Vending machine code must not empty!"))
                    } else if (portCashBox.isEmpty()) {
                        sendEvent(Event.Toast("Port cash box must not empty!"))
                    } else if (portVendingMachine.isEmpty()) {
                        sendEvent(Event.Toast("Port vending machine must not empty!"))
                    } else if (typeVendingMachine.isEmpty()) {
                        sendEvent(Event.Toast("Type vending machine must not empty!"))
                    } else if (loginRequest.username.isEmpty()) {
                        sendEvent(Event.Toast("Username must not empty!"))
                    } else if (loginRequest.password.isEmpty()) {
                        sendEvent(Event.Toast("Password must not empty!"))
                    } else {
                        val passwordEncode = authRepository.encodePassword(loginRequest.password)
                        logger.info("passwordEncode: $passwordEncode")
                        val passwordDecode = authRepository.decodePassword(passwordEncode)
                        logger.info("passwordDecode: $passwordDecode")
                        val response = authRepository.login(inputVendingMachineCode, loginRequest)
                        if (response.accessToken.isNotEmpty()) {
                            val logAuthy = LogAuthy(
                                machineCode = inputVendingMachineCode,
                                authyType = "login",
                                username = loginRequest.username,
                                eventTime = LocalDateTime.now().toDateTimeString(),
                            )
                            baseRepository.addNewLogToLocal(
                                eventType = "authy",
                                severity = "normal",
                                eventData = logAuthy,
                            )
                            val baudRateCashBox = "9600"
                            val baudRateVendingMachine = "9600"
                            val initSetup = InitSetup(
                                vendCode = inputVendingMachineCode,
                                androidId = baseRepository.getAndroidId(),
                                username = loginRequest.username,
                                password = passwordDecode,
                                portVendingMachine = portVendingMachine,
                                baudRateVendingMachine = baudRateVendingMachine,
                                portCashBox = portCashBox,
                                baudRateCashBox = baudRateCashBox,
                                typeVendingMachine = typeVendingMachine,
                                fullScreenAds = "ON",
                                withdrawalAllowed = "ON",
                                autoStartApplication = "ON",
                                layoutHomeScreen = "3",
                                timeTurnOnLight = "",
                                timeTurnOffLight = "",
                                dropSensor = "ON",
                                inchingMode = "0",
                                timeToJumpToAdsScreen = "60",
                                glassHeatingMode = "ON",
                                highestTempWarning = "30",
                                lowestTempWarning = "0",
                                temperature = "",
                                timeoutPayment = "60",
                                role = ""
                            )
                            baseRepository.writeDataToLocal(
                                data = initSetup,
                                path = pathFileInitSetup
                            )
                            if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox) == -1) {
                                logger.info("Open port cash box is error!")
                            } else {
                                logger.info("Open port cash box is success")
                                portConnectionDataSource.startReadingCashBox()
                            }
                            if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine) == -1) {
                                logger.info("Open port vending machine is error!")
                            } else {
                                logger.info("Open port vending machine is success")
                                portConnectionDataSource.startReadingVendingMachine()
                            }
                            val logSetup = LogSetup(
                                machineCode = inputVendingMachineCode,
                                operationContent = "setup init: $initSetup",
                                operationType = "setup",
                                username = loginRequest.username,
                                eventTime = LocalDateTime.now().toDateTimeString(),
                            )
                            baseRepository.addNewLogToLocal(
                                eventType = "setup",
                                severity = "normal",
                                eventData = logSetup,
                            )
                            sendEvent(Event.Toast("Setup init success"))
                            navController.popBackStack()
                            navController.navigate(Screens.SettingScreenRoute.route)
                        } else {
                            val logError = LogError(
                                machineCode = inputVendingMachineCode,
                                errorType = "application",
                                errorContent = "access token get from api is empty in InitSetupViewModel/writeInitSetupToLocal()",
                                eventTime = LocalDateTime.now().toDateTimeString(),
                            )
                            baseRepository.addNewLogToLocal(
                                eventType = "error",
                                severity = "normal",
                                eventData = logError,
                            )
                            sendEvent(Event.Toast("Access token is empty!"))
                        }
                    }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val logError = LogError(
                    machineCode = "",
                    errorType = "application",
                    errorContent = "write init setup to local in the first time fail in InitSetupViewModel/writeInitSetupToLocal(): ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}