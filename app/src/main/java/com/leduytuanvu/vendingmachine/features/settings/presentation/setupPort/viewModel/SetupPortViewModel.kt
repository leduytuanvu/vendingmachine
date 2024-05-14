package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewState.SetupPortViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SetupPortViewModel @Inject constructor(
    private val baseRepository: BaseRepository,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupPortViewState())
    val state = _state.asStateFlow()
    init {
        getInitSetupFromLocal()
    }
    private fun getInitSetupFromLocal() {
        logger.debug("getInitSetupFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )
                if (initSetup != null) {
                    _state.update {
                        it.copy(
                            initSetup = initSetup,
                            isLoading = false
                        )
                    }
                } else {
                    val logError = LogError(
                        machineCode = "",
                        errorType = "application",
                        errorContent = "init setup from local is null",
                        eventTime = LocalDateTime.now().toDateTimeString(),
                    )
                    baseRepository.addNewLogToLocal(
                        eventType = "error",
                        severity = "normal",
                        eventData = logError,
                    )
                }
            } catch (e: Exception) {
                val logError = LogError(
                    machineCode = "",
                    errorType = "application",
                    errorContent = "get init setup from local fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast(e.message!!))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // DONE
    fun saveSetupPort(
        typeVendingMachine: String,
        portCashBox: String,
        portVendingMachine: String,
    ) {
        logger.debug("saveSetupPort")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                if (portCashBox == portVendingMachine) {
                    sendEvent(Event.Toast("Port cash box and port vending machine must not same!"))
                } else {
                    delay(1000)
                    if (portConnectionDataSource.openPortCashBox(portCashBox) == -1) {
                        throw Exception("Open port cash box is error!")
                    } else {
                        portConnectionDataSource.startReadingCashBox()
                    }
                    if (portConnectionDataSource.openPortVendingMachine(portVendingMachine) == -1) {
                        throw Exception("Open port vending machine is error!")
                    } else {
                        portConnectionDataSource.startReadingVendingMachine()
                    }
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    initSetup.typeVendingMachine = typeVendingMachine
                    initSetup.portCashBox = portCashBox
                    initSetup.portVendingMachine = portVendingMachine
                    baseRepository.writeDataToLocal(
                        data = initSetup,
                        path = pathFileInitSetup,
                    )
                    val logSetup = LogSetup(
                        machineCode = initSetup.vendCode,
                        operationContent = "set: $initSetup",
                        operationType = "setup port",
                        username = initSetup.username,
                        eventTime = LocalDateTime.now().toDateTimeString(),
                    )
                    baseRepository.addNewLogToLocal(
                        eventType = "setup",
                        severity = "normal",
                        eventData = logSetup,
                    )
                    sendEvent(Event.Toast("Setup port success"))
                    _state.update {
                        it.copy(
                            initSetup = initSetup,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "save setup port fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}