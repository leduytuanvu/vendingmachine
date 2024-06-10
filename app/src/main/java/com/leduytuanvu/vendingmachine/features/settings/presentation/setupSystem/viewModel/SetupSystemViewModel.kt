package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.ScheduledTaskWorker
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.TypeTXCommunicateAvf
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SetupSystemViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val byteArrays: ByteArrays,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupSystemViewState())
    val state = _state.asStateFlow()

    private val _nameFun = MutableStateFlow<String>("")
    val nameFun: StateFlow<String> = _nameFun.asStateFlow()

    private val _statusVendingMachine = MutableStateFlow(false)
    val statusVendingMachine: StateFlow<Boolean> = _statusVendingMachine.asStateFlow()

    private val workManager = WorkManager.getInstance(context)

    private var vendingMachineJob: Job? = null

    fun loadInitData() {
        logger.debug("loadInitData")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                val serialSimId = settingsRepository.getSerialSimId()
                portConnectionDatasource.openPortVendingMachine(initSetup.portVendingMachine)
                if(!portConnectionDatasource.checkPortVendingMachineStillStarting()) {
                    portConnectionDatasource.startReadingVendingMachine()
                }
                portConnectionDatasource.startReadingVendingMachine()
                startCollectingData()
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArrays.vmReadTemp,

                )
                if (baseRepository.isHaveNetwork(context)) {
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(
                        initSetup = initSetup,
                        serialSimId = serialSimId,
                        informationOfMachine = informationOfMachine,
                        isLoading = false,
                    ) }
                } else {
                    _state.update { it.copy(
                        initSetup = initSetup,
                        serialSimId = serialSimId,
                        isLoading = false,
                    ) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get init information machine from server fail in SetupSystemViewModel/loadInitData(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun closePort() {
        vendingMachineJob?.cancel()
        vendingMachineJob = null
        portConnectionDatasource.closeVendingMachinePort()
    }

    fun startCollectingData() {
        vendingMachineJob = viewModelScope.launch {
            portConnectionDatasource.dataFromCashBox.collect { data ->
                processingDataFromVendingMachine(data)
            }
        }
    }

    fun processingDataFromVendingMachine(dataByteArray: ByteArray) {
        val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
        logger.debug(dataHexString)
        if(dataHexString.contains("00,5D,00,00,5D")) {
            if(_nameFun.value == "checkDropSensor") {
                sendEvent(Event.Toast("The drop sensor works normally"))
            } else if(_nameFun.value == "updateInchingModeInLocal") {
                _statusVendingMachine.value = true
            } else if(_nameFun.value == "updateGlassHeatingModeInLocal") {
                _statusVendingMachine.value = true
            } else if(_nameFun.value == "updateTemperatureInLocal") {
                _statusVendingMachine.value = true
            }
        } else if(dataHexString.contains("00,5C,10,00,6C")) {
            sendEvent(Event.Toast("PMOS is short circuited"))
        } else if(dataHexString.contains("00,5C,02,00,5E")) {
            sendEvent(Event.Toast("The drop sensor is turned off"))
        } else if(dataHexString.contains("00,5C,00,00,5C")) {
            _statusVendingMachine.value = false
        }

        if(dataByteArray.size==5) {
            if(dataByteArray[0] == 0x00.toByte()
                && dataByteArray[1] == 0x5D.toByte()
                && (dataByteArray[4] == 0x66.toByte()
                        || dataByteArray[4] == 0x65.toByte()
                        || dataByteArray[4] == 0x6B.toByte()
                        || dataByteArray[4] == 0x67.toByte()
                        || dataByteArray[4] == 0x63.toByte()
                        || dataByteArray[4] == 0x64.toByte())
            ) {
//                logger.debug("============================== data: $dataHexString")
                if(dataByteArray[2] == 0xEB.toByte()) {
                    _state.update { it.copy(temp1 = "không thể kết nối") }
                } else {
                    _state.update { it.copy(temp1 = "${dataByteArray[2].toInt()}") }
                }
                if(dataByteArray[3] == 0xEB.toByte()) {
                    _state.update { it.copy(temp2 = "không thể kết nối") }
                } else {
                    _state.update { it.copy(temp2 = "${dataByteArray[3].toInt()}") }
                }
            }
        }
    }

    fun checkDropSensor() {
        logger.debug("checkDropSensor")
        viewModelScope.launch {
            try {
                _nameFun.value = "checkDropSensor"
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArrays.vmCheckDropSensor,

                )
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "check drop sensor fail in SetupSystemViewModel/checkDropSensor(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getTemp() {
        logger.debug("getTemp")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArrays.vmReadTemp,

                )
                delay(1000)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get temperature fail in SetupSystemViewModel/getTemp(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getInitSetupFromLocal() {
        logger.debug("getInitSetupFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _state.update { it.copy(initSetup = initSetup) }
            } catch (e: Exception) {
                sendEvent(Event.Toast(e.message!!))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateVendCodeInLocal(newVendCode: String) {
        logger.debug("updateVendCodeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.vendCode = newVendCode
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update machine code to ${initSetup.vendCode}",
                    operationType = "setup machine code",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update machine code fail in SetupSystemViewModel/updateVendCodeInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateFullScreenAdsInLocal(fullScreenAds: String) {
        logger.debug("updateFullScreenAdsInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.fullScreenAds = fullScreenAds
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update full screen ads to ${initSetup.fullScreenAds}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update full screen ads fail in SetupSystemViewModel/updateFullScreenAdsInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateWithdrawalAllowedInLocal(withdrawalAllowed: String) {
        logger.debug("updateWithdrawalAllowedInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.withdrawalAllowed = withdrawalAllowed
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update withdrawal allowed to ${initSetup.withdrawalAllowed}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update withdrawal allowed fail in SetupSystemViewModel/updateWithdrawalAllowedInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateAutoStartApplicationInLocal(autoStartApplication: String) {
        logger.debug("updateAutoStartApplicationInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.autoStartApplication = autoStartApplication
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update auto start application to ${initSetup.autoStartApplication}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update auto start application fail in SetupSystemViewModel/updateAutoStartApplicationInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateLayoutHomeInLocal(layoutHomeScreen: String) {
        logger.debug("updateLayoutHomeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.layoutHomeScreen = layoutHomeScreen
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update layout home screen to ${initSetup.layoutHomeScreen}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update layout home screen fail in SetupSystemViewModel/updateLayoutHomeInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTimeTurnOnTurnOffLightInLocal(timeTurnOnLight: String, timeTurnOffLight: String) {
        logger.debug("updateTimeTurnOnTurnOffLightInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.timeTurnOnLight = timeTurnOnLight
                initSetup.timeTurnOffLight = timeTurnOffLight
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update time turn on light to ${initSetup.timeTurnOnLight} and time turn off light to ${initSetup.timeTurnOffLight}",
                    operationType = "setup system",
                    username = initSetup.username,
                )

                val partsTimeTurnOnLight = initSetup.timeTurnOnLight.split(":")
                val hourTurnOnLight = partsTimeTurnOnLight[0].toInt()
                val minuteTurnOnLight = partsTimeTurnOnLight[1].toInt()
                rescheduleDailyTask("TurnOnLightTask", hourTurnOnLight, minuteTurnOnLight)

                val partsTimeTurnOffLight = initSetup.timeTurnOffLight.split(":")
                val hourTurnOffLight = partsTimeTurnOffLight[0].toInt()
                val minuteTurnOffLight = partsTimeTurnOffLight[1].toInt()
                rescheduleDailyTask("TurnOffLightTask", hourTurnOffLight, minuteTurnOffLight)

                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update time turn on light and time turn off light fail in SetupSystemViewModel/updateTimeTurnOnTurnOffLightInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun rescheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        try {
            workManager.cancelUniqueWork(taskName).also {
                Logger.debug("Delete task scheduled $taskName and make scheduled again")
                scheduleDailyTask(taskName, hour, minute)
            }
        } catch (e: Exception) {
            throw  e
        }
    }

    private fun scheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        try {
            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(currentTime)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

            val inputData = Data.Builder()
                .putString("TASK_NAME", taskName)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<ScheduledTaskWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork(
                taskName, // Unique task name
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateDropSensorInLocal(dropSensor: String) {
        logger.debug("updateDropSensorInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.dropSensor = dropSensor
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update drop sensor to ${initSetup.dropSensor}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update drop sensor fail in SetupSystemViewModel/updateDropSensorInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateInchingModeInLocal(inchingMode: String) {
        logger.debug("updateInchingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _nameFun.value = "updateInchingModeInLocal"
                _statusVendingMachine.value = false
                var byteArray: ByteArray = when (inchingMode) {
                    "0" -> byteArrays.vmInchingMode0
                    "1" -> byteArrays.vmInchingMode1
                    "2" -> byteArrays.vmInchingMode2
                    "3" -> byteArrays.vmInchingMode3
                    "4" -> byteArrays.vmInchingMode4
                    "5" -> byteArrays.vmInchingMode5
                    else -> { byteArrays.vmInchingMode0 }
                }
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArray,

                )
                delay(1100)
                if(_statusVendingMachine.value) {
                    initSetup.inchingMode = inchingMode
                    baseRepository.addNewSetupLogToLocal(
                        machineCode = initSetup.vendCode,
                        operationContent = "set inching mode to ${initSetup.inchingMode}",
                        operationType = "setup system",
                        username = initSetup.username,
                    )
                    baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                    sendEvent(Event.Toast("SUCCESS"))
                    _state.update { it.copy(
                        initSetup = initSetup,
                        isLoading = false,
                    ) }
                } else {
                    byteArray = when (initSetup.inchingMode) {
                        "0" -> byteArrays.vmInchingMode0
                        "1" -> byteArrays.vmInchingMode1
                        "2" -> byteArrays.vmInchingMode2
                        "3" -> byteArrays.vmInchingMode3
                        "4" -> byteArrays.vmInchingMode4
                        "5" -> byteArrays.vmInchingMode5
                        else -> { byteArrays.vmInchingMode0 }
                    }
                    portConnectionDatasource.sendCommandVendingMachine(
                        byteArray,

                    )
                    sendEvent(Event.Toast("Setup inching mode failed!"))
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update inching mode fail in SetupSystemViewModel/updateInchingModeInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTimeJumpToAdsScreenInLocal(timeToJumpToAdsScreen: String) {
        logger.debug("updateTimeJumpToAdsScreenInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.timeoutJumpToBigAdsScreen = timeToJumpToAdsScreen
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update time jump to ads screen to ${initSetup.timeoutJumpToBigAdsScreen}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update time jump to ads screen fail in SetupSystemViewModel/updateTimeJumpToAdsScreenInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateGlassHeatingModeInLocal(glassHeatingMode: String) {
        logger.debug("updateGlassHeatingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _nameFun.value = "updateGlassHeatingModeInLocal"
                _statusVendingMachine.value = false
                var byteArray: ByteArray = when (glassHeatingMode) {
                    "ON" -> byteArrays.vmTurnOnGlassHeatingMode
                    "OFF" -> byteArrays.vmTurnOffGlassHeatingMode
                    else -> { byteArrays.vmTurnOnGlassHeatingMode }
                }
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArray,

                )
                delay(1100)
                if(_statusVendingMachine.value) {
                    initSetup.glassHeatingMode = glassHeatingMode
                    baseRepository.addNewSetupLogToLocal(
                        machineCode = initSetup.vendCode,
                        operationContent = "update glass heating mode to ${initSetup.glassHeatingMode}",
                        operationType = "setup system",
                        username = initSetup.username,
                    )
                    baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                    sendEvent(Event.Toast("SUCCESS"))
                    _state.update { it.copy(
                        initSetup = initSetup,
                        isLoading = false,
                    ) }
                } else {
                    byteArray = when (initSetup.glassHeatingMode) {
                        "ON" -> byteArrays.vmTurnOnGlassHeatingMode
                        "OFF" -> byteArrays.vmTurnOffGlassHeatingMode
                        else -> { byteArrays.vmTurnOnGlassHeatingMode }
                    }
                    portConnectionDatasource.sendCommandVendingMachine(
                        byteArray,

                    )
                    sendEvent(Event.Toast("Setup glass heating mode failed!"))
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update glass heating mode fail in SetupSystemViewModel/updateGlassHeatingModeInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateHighestAndLowestTempWarningInLocal(highestTempWarning: String, lowestTempWarning: String) {
        logger.debug("updateHighestTempWarningInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.highestTempWarning = highestTempWarning
                initSetup.lowestTempWarning = lowestTempWarning
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update highest temp warning to ${initSetup.highestTempWarning}",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update highest temp warning fail in SetupSystemViewModel/updateHighestTempWarningInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateLowestTempWarningInLocal(lowestTempWarning: String) {
        logger.debug("updateLowestTempWarningInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.lowestTempWarning = lowestTempWarning
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

//    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
//        viewModelScope.launch {
//            _state.update { it.copy(
//                isConfirm = true,
//                titleDialogConfirm = mess,
//            ) }
//        }
//    }
//
//    fun showDialogConfirm(mess: String) {
//        viewModelScope.launch {
//            _state.update {
//                it.copy(
//                    titleDialogConfirm = mess,
//                    isConfirm = true,
//                )
//            }
//        }
//    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConfirm = false,
                    titleDialogConfirm = "",
                )
            }
        }
    }

    private fun generateTargetTemperatureByteArray(temperature: Int): ByteArray {
        try {
            require(temperature in 0..255) { "Temperature must be in the range 0 to 255" }

            // Define the fixed part of the byte array
            val prefix = byteArrayOf(0x00.toByte(), 0xFF.toByte(), 0xDC.toByte(), 0x23.toByte())
            // Add the temperature byte
            val tempByte = temperature.toByte()

            // Calculate the checksum
            val sum = prefix.sumOf { it.toInt() and 0xFF } + (tempByte.toInt() and 0xFF)
            val checksum = (0xFF - (sum and 0xFF)).toByte()

            // Construct the final byte array
            return prefix + tempByte + checksum
        } catch (e: Exception) {
            throw e
        }
    }

    fun updateTemperatureInLocal(temperature: String) {
        logger.debug("updateTemperatureInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _nameFun.value = "updateTemperatureInLocal"
                _statusVendingMachine.value = false
                portConnectionDatasource.sendCommandVendingMachine(
                    generateTargetTemperatureByteArray(temperature.toInt()),

                )
                delay(1100)
                if(_statusVendingMachine.value) {
                    initSetup.temperature = temperature
                    baseRepository.addNewSetupLogToLocal(
                        machineCode = initSetup.vendCode,
                        operationContent = "update temperature to ${initSetup.temperature}",
                        operationType = "setup system",
                        username = initSetup.username,
                    )
                    baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                    sendEvent(Event.Toast("SUCCESS"))
                    _state.update { it.copy(
                        initSetup = initSetup,
                        isLoading = false,
                    ) }
                } else {
                    portConnectionDatasource.sendCommandVendingMachine(
                        generateTargetTemperatureByteArray(initSetup.temperature.toInt()),

                    )
                    sendEvent(Event.Toast("Setup temperature failed!"))
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update temperature fail in SetupSystemViewModel/updateTemperatureInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetFactory(navController: NavHostController,) {
        logger.debug("resetFactory")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.deleteFile(pathFileInitSetup)
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "reset factory",
                    operationType = "setup system",
                    username = initSetup.username,
                )
                _state.update { it.copy(isLoading = false) }
                navController.popBackStack()
                navController.popBackStack()
                navController.navigate(Screens.SplashScreenRoute.route)
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "reset factory fail in SetupSystemViewModel/resetFactory(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getSerialSimId() {
        logger.debug("getSerialSimId")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val serialSimId = settingsRepository.getSerialSimId()
                _state.update { it.copy(
                    serialSimId = serialSimId,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "set serial sim id fail in SetupSystemViewModel/getSerialSimId(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
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

    fun getInformationOfMachine() {
        logger.debug("getInformationOfMachine")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(
                        informationOfMachine = informationOfMachine,
                        isLoading = false,
                    ) }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get information machine from server fail: ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    fun onLight() {
        portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOnLight)
    }

    fun offLight() {
        portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOffLight)
    }
}