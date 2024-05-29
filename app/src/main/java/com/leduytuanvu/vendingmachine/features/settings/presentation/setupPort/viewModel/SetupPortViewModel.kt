package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
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

    fun loadInitSetup() {
        logger.debug("loadInit")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _state.update {
                    it.copy (
                        initSetup = initSetup,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = "",
                    errorContent = "load init fail in SetupPortViewModel/loadInit(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

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
                    var openPortCashBox = true
                    var openPortVendingMachine = true
                    delay(500)
                    if (portConnectionDataSource.openPortCashBox(portCashBox) == -1) {
                        openPortCashBox = false
                    } else {
                        portConnectionDataSource.closeCashBoxPort()
                    }
                    if (portConnectionDataSource.openPortVendingMachine(portVendingMachine) == -1) {
                        openPortVendingMachine = false
                    } else {
                        portConnectionDataSource.closeVendingMachinePort()
                    }
                    if(!openPortCashBox || !openPortVendingMachine) {
                        sendEvent(Event.Toast("Setup port for cash box and vending machine fail!"))
                        _state.update { it.copy(isLoading = false) }
                    } else {
                        val initSetup: InitSetup = baseRepository.getDataFromLocal(
                            type = object : TypeToken<InitSetup>() {}.type,
                            path = pathFileInitSetup,
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
                            operationContent = "setup port: $initSetup",
                            operationType = "setup",
                            username = initSetup.username,
                            eventTime = LocalDateTime.now().toDateTimeString(),
                        )
                        baseRepository.addNewLogToLocal(
                            eventType = "setup",
                            severity = "normal",
                            eventData = logSetup,
                        )
                        baseRepository.addNewSetupLogToLocal(
                            machineCode = initSetup.vendCode,
                            operationContent = "setup port: $initSetup",
                            operationType = "setup port",
                            username = initSetup.username,
                        )
                        sendEvent(Event.Toast("Setup port success"))
                        _state.update {
                            it.copy (
                                initSetup = initSetup,
                                isLoading = false,
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "save setup port fail in SetupPortViewModel/saveSetupPort(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}