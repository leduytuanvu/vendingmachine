package com.combros.vendingmachine.features.settings.presentation.setupSystem.viewModel

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.ScheduledTaskWorker
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.core.util.ByteArrays
import com.combros.vendingmachine.core.util.Event
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.core.util.sendEvent
import com.combros.vendingmachine.features.auth.data.model.request.LoginRequest
import com.combros.vendingmachine.features.auth.domain.repository.AuthRepository
import com.combros.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.combros.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.io.ByteArrayOutputStream

import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// Command Definitions
const val CMD_QUERY_VMC_STATUS = 0x00
const val CMD_VMC_DELIVER = 0x01
const val CMD_VMC_RETURN_CHANGE = 0x02
const val CMD_QUERY_VMC_PARAMS = 0x03
const val CMD_SET_VMC_PARAMS = 0x04
const val CMD_PERIPHERAL_CONTROL = 0x05
const val CMD_CONTENT_DELIVERY = 0x0A
const val CMD_UPDATE_BOARD_CODE = 0x10

const val START_BYTE: Byte = 0x02
const val END_BYTE: Byte = 0x03
const val ID: Byte = 0x00

@HiltViewModel
class SetupSystemViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
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

    private val _sn = MutableStateFlow<Int>(0)
    val sn: StateFlow<Int> = _sn.asStateFlow()

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
                portConnectionDatasource.openPortVendingMachine(initSetup.portVendingMachine,initSetup.typeVendingMachine)
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
            portConnectionDatasource.dataFromVendingMachine.collect { data ->
                processingDataFromVendingMachine(data)
            }
        }
    }

    fun processingDataFromVendingMachine(dataByteArray: ByteArray) {
        val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
//        logger.debug(dataHexString)
        if(dataHexString.contains("00,5D,00,00,5D")) {
            if(_nameFun.value == "checkDropSensor") {
                sendEvent(Event.Toast("The drop sensor works normally"))
            } else if(_nameFun.value == "updateInchingModeInLocal") {
                _statusVendingMachine.value = true
            } else if(_nameFun.value == "updateGlassHeatingModeInLocal") {
                _statusVendingMachine.value = true
            } else if(_nameFun.value == "updateTemperatureInLocal") {
                _statusVendingMachine.value = true
            } else if(_nameFun.value == "setTemp") {
                _statusVendingMachine.value = true
            }  else if(_nameFun.value == "turnOffLight" || _nameFun.value == "turnOnLight") {
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

    fun saveSetTimeResetOnEveryDay(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.timeResetOnEveryDay = "$hour:$minute"
                rescheduleDailyTask("ResetAppTask", hour, minute)
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "save time reset on every day ${hour}:${minute}",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "save default promotion fail in SetupPaymentViewModel/saveDefaultPromotion(): ${e.message}",
                )
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
                val dataPassword = Base64.decode(initSetup.password, Base64.DEFAULT)
                val loginRequest = LoginRequest(initSetup.username,String(dataPassword, Charsets.UTF_8).substringBefore("567890VENDINGMACHINE", ""))
                val response = authRepository.login(newVendCode,loginRequest)
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

    fun updateAutoTurnOnTurnOffLightInLocal(typeAutoTurnOnTurnOffLight: String) {
        logger.debug("updateAutoTurnOnTurnOffLightInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.autoTurnOnTurnOffLight = typeAutoTurnOnTurnOffLight
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update auto turn on turn off light to ${initSetup.autoTurnOnTurnOffLight}",
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
                    errorContent = "update auto turn on turn off light fail in SetupSystemViewModel/updateAutoTurnOnTurnOffLightInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateAutoResetAppEveryDayInLocal(typeAutoResetAppEveryDay: String) {
        logger.debug("updateAutoResetAppEveryDayInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.autoResetAppEveryday = typeAutoResetAppEveryDay
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update auto reset app everyday to ${initSetup.autoTurnOnTurnOffLight}",
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
                    errorContent = "update auto reset app everyday fail in SetupSystemViewModel/updateAutoResetAppEveryDayInLocal(): ${e.message}",
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

    fun updateBigAdsInLocal(bigAds: String) {
        logger.debug("updateBigAdsInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.fullScreenAds = bigAds
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update big ads: ${initSetup.fullScreenAds}",
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
                    errorContent = "update big ads fail in SetupSystemViewModel/updateTimeJumpToAdsScreenInLocal(): ${e.message}",
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
                _nameFun.value = "setTemp"
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _nameFun.value = "updateTemperatureInLocal"
                _statusVendingMachine.value = false
                val byteArray1 = byteArrayOf(0x00, 0xFF.toByte(), 0xCC.toByte(), 0x33.toByte(), 0x00.toByte(), 0xFF.toByte())
                portConnectionDatasource.sendCommandVendingMachine(byteArray1)
                delay(1001)
                if(_statusVendingMachine.value) {
                    _statusVendingMachine.value = false
                    if(initSetup.glassHeatingMode == "ON") {
                        val byteArray2 = byteArrayOf(0x00, 0xFF.toByte(), 0xCD.toByte(), 0x32.toByte(), 0x00.toByte(), 0xFF.toByte())
                        portConnectionDatasource.sendCommandVendingMachine(byteArray2)
                    } else {
                        val byteArray2 = byteArrayOf(0x00, 0xFF.toByte(), 0xCD.toByte(), 0x32.toByte(), 0x01.toByte(), 0xFE.toByte())
                        portConnectionDatasource.sendCommandVendingMachine(byteArray2)
                    }
                    delay(1001)
                    if(_statusVendingMachine.value) {
                        _statusVendingMachine.value = false
//                        val byteArray3 = byteArrayOf(0x00, 0xFF.toByte(), 0xCE.toByte(), 0x31.toByte(), 0x05.toByte(), 0xFA.toByte())
                        val numberBoard = 0
                        val byteNumberBoard: Byte = numberBoard.toByte()
                        val byteArray3 = byteArrayOf(
                            byteNumberBoard,
                            (0xFF - numberBoard).toByte(),
                            0xCE.toByte(),
                            0x31,
                            temperature.toByte(),
                            (0xFF - temperature.toInt()).toByte(),
                        )
                        portConnectionDatasource.sendCommandVendingMachine(byteArray3)
                        delay(1001)
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
                            ) }
                        } else {
                            sendEvent(Event.Toast("Set temperature failed!"))
                        }
                    } else {
                        sendEvent(Event.Toast("Set temperature failed!"))
                    }
                } else {
                    sendEvent(Event.Toast("Set temperature failed!"))
                }

//                if(_statusVendingMachine.value) {
//                    initSetup.temperature = temperature
//                    baseRepository.addNewSetupLogToLocal(
//                        machineCode = initSetup.vendCode,
//                        operationContent = "update temperature to ${initSetup.temperature}",
//                        operationType = "setup system",
//                        username = initSetup.username,
//                    )
//                    baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
//                    sendEvent(Event.Toast("SUCCESS"))
//                    _state.update { it.copy(
//                        initSetup = initSetup,
//                        isLoading = false,
//                    ) }
//                } else {
//                    portConnectionDatasource.sendCommandVendingMachine(
//                        generateTargetTemperatureByteArray(initSetup.temperature.toInt()),
//
//                    )
//                    sendEvent(Event.Toast("Setup temperature failed!"))
//                    _state.update { it.copy(isLoading = false) }
//                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update temperature fail in SetupSystemViewModel/updateTemperatureInLocal(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetFactory(navController: NavHostController) {
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

    fun turnOnLight() {
        viewModelScope.launch {
            _nameFun.value = "turnOnLight"
            _statusVendingMachine.value = false
            portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOnLight)
            delay(1001)
            if(_statusVendingMachine.value) {
                sendEvent(Event.Toast("SUCCESS"))
            } else {
                sendEvent(Event.Toast("FAIL"))
            }
        }
    }

    fun turnOffLight() {
        viewModelScope.launch {
            _nameFun.value = "turnOffLight"
            _statusVendingMachine.value = false
            portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOffLight)
            delay(1001)
            if(_statusVendingMachine.value) {
                sendEvent(Event.Toast("SUCCESS"))
            } else {
                sendEvent(Event.Toast("FAIL"))
            }
        }
    }


    // CRC16 Tables
    val CRC16_TAB_H = intArrayOf(
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
    )

    val CRC16_TAB_L = intArrayOf(
        0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2, 0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04,
        0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8,
        0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC,
        0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12, 0x13, 0xD3, 0x11, 0xD1, 0xD0, 0x10,
        0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
        0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE, 0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38,
        0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C,
        0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26, 0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0,
        0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4,
        0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
        0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA, 0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C,
        0xB4, 0x74, 0x75, 0xB5, 0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0,
        0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92, 0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54,
        0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98,
        0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
        0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86, 0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40
    )

    // CRC16 Calculation
    fun calculateCrc16(data: ByteArray): Int {
        var crcHi = 0xFF
        var crcLo = 0xFF
        for (byte in data) {
            val index = crcLo.xor(byte.toInt()) and 0xFF
            crcLo = crcHi.xor(CRC16_TAB_H[index.toInt()])
            crcHi = CRC16_TAB_L[index.toInt()]
        }
        return (crcHi.toInt() shl 8) or crcLo.toInt()
    }

    // Function to generate command packet
//    fun generateCommandPacket(cmd: Byte, data: String): ByteArray {
//        val startByte: Byte = 0x02
//        val endByte: Byte = 0x03
//        val idByte: Byte = 0x00
//        val snByte: Byte = 0x00 // Assuming initial SN is 0
//        val cmdBytes = byteArrayOf(cmd)
//        val dataLengthBytes = byteArrayOf((data.length shr 8).toByte(), (data.length and 0xFF).toByte())
//        val packet = byteArrayOf(startByte) + dataLengthBytes + cmdBytes + idByte + snByte + data.toByteArray() + endByte
//        val crc = calculateCrc16(packet)
//        val crcBytes = byteArrayOf((crc shr 8).toByte(), (crc and 0xFF).toByte())
//        return packet + crcBytes
//    }

    // Define the LED command code
    val CMD_TURN_ON_LED: Byte = 0x01

    // Define the LED number or color (replace "00" with the appropriate value)
    val ledNumberOrColor = "00"

    // Function to generate the command packet
    fun generateCommandPacket(command: Byte, parameter: String): ByteArray {
        // Construct the byte array packet
        // Example: [START_BYTE, CMD_TURN_ON_LED, PARAM_LENGTH, PARAM_DATA, CHECKSUM, END_BYTE]
        val startByte: Byte = 0x02
        val endByte: Byte = 0x03
        val paramLength: Byte = parameter.length.toByte()
        val checksum: Byte = calculateChecksum(command, parameter)

        return byteArrayOf(startByte, command, paramLength) +
                parameter.toByteArray(Charsets.UTF_8) +
                byteArrayOf(checksum, endByte)
    }

    // Function to calculate checksum
    fun calculateChecksum(command: Byte, parameter: String): Byte {
        // Logic to calculate the checksum (implementation depends on documentation)
        // Example: XOR of all bytes in the command packet
        var checksum = command.toInt()
        parameter.forEach { checksum = checksum xor it.toInt() }
        return checksum.toByte()
    }

//    fun calculateCRC16(data: ByteArray): ByteArray {
//        val crc = CRC32()
//        crc.update(data)
//        val checksum = crc.value.toInt()
//        val checksumBytes = ByteBuffer.allocate(4).putInt(checksum).array()
//        return byteArrayOf(checksumBytes[2], checksumBytes[3]) // Return the last two bytes
//    }

//    fun generateCommandPacket(command: Byte, sn: Byte, data: Map<String, Any>) {
//        viewModelScope.launch {
//            try {
//                // Serialize data to JSON string
//                val jsonData = "{\"LEDL\": 1}"
//                logger.debug("json data: $jsonData")
//                val jsonDataBytes = jsonData.toByteArray()
//                logger.debug("json data bytes: ${baseRepository.byteArrayToHexString(jsonDataBytes)}")
//                // Calculate data length
//                val dataLength = 1 + 1 + 1 + jsonDataBytes.size // CMD + ID + SN + JSON data length
//                logger.debug("data length: $dataLength")
//                val dataLengthBytes = ByteBuffer.allocate(2).putShort(dataLength.toShort()).array()
//                logger.debug("data length bytes: ${baseRepository.byteArrayToHexString(dataLengthBytes)}")
//                val packet = concatByteArrays(
//                    byteArrayOf(START_BYTE),
//                    dataLengthBytes,
//                    byteArrayOf(command),
//                    byteArrayOf(ID),
//                    byteArrayOf(sn),
//                    jsonDataBytes,
//                    byteArrayOf(END_BYTE)
//                )
//                logger.debug("packet: ${baseRepository.byteArrayToHexString(packet)}")
//
//                // Calculate checksum
//                val checksum = calculateCRC16(packet)
//                logger.debug("checksum: ${baseRepository.byteArrayToHexString(checksum)}")
//
//                // Combine packet and checksum
//                val byteArrayResult = packet + checksum
//                logger.debug("byte array result: ${baseRepository.byteArrayToHexString(byteArrayResult)}")
//
//                // Send the command to the port
//                portConnectionDatasource.sendCommandVendingMachine(byteArrayResult)
//            } catch (e: Exception) {
//                logger.error("error: ${e.message}")
//            }
//        }
//    }

//    fun generateCommandPacket(command: Byte, sn: Byte, data: Map<String, Any>) {
//        viewModelScope.launch {
//            try {
//                // Serialize data to JSON string
//                val jsonData = "{\"LEDL\": 1}"
//                logger.debug("json data: $jsonData")
//                val jsonDataBytes = jsonData.toByteArray(Charsets.UTF_8) // Use UTF-8 encoding
//                logger.debug("json data bytes: ${baseRepository.byteArrayToHexString(jsonDataBytes)}")
//                // Calculate data length
//                val dataLength = 1 + 1 + 1 + jsonDataBytes.size // CMD + ID + SN + JSON data length
//                logger.debug("data length: $dataLength")
//                val dataLengthBytes = ByteBuffer.allocate(2).putShort(dataLength.toShort()).array()
//                logger.debug("data length bytes: ${baseRepository.byteArrayToHexString(dataLengthBytes)}")
//                val packet = concatByteArrays(
//                    byteArrayOf(START_BYTE),
//                    dataLengthBytes,
//                    byteArrayOf(command),
//                    byteArrayOf(ID),
//                    byteArrayOf(sn),
//                    jsonDataBytes,
//                    byteArrayOf(END_BYTE)
//                )
//                logger.debug("packet: ${baseRepository.byteArrayToHexString(packet)}")
//
//                // Calculate checksum
//                val checksum = calculateCRC16(packet, dataLength)
//                logger.debug("checksum: ${baseRepository.byteArrayToHexString(checksum)}")
//
//                // Combine packet and checksum
//                val byteArrayResult = packet + checksum
//                logger.debug("byte array result: ${baseRepository.byteArrayToHexString(byteArrayResult)}")
//
//                // Send the command to the port
//                portConnectionDatasource.sendCommandVendingMachine(byteArrayResult)
//            } catch (e: Exception) {
//                logger.error("error: ${e.message}")
//            }
//        }
//    }

    fun concatByteArrays(vararg arrays: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        for (array in arrays) {
            outputStream.write(array)
        }
        return outputStream.toByteArray()
    }

//    fun setVendingMachineParams(tmode: Int, target: Int, frost: Int, twork: Int, ledl: Int, lpower: Int, glhl: Int, gpower: Int, coinEnable: Int, coinChange: Int, billEnable: Int, billChange: Int, billEcrow: Int, billChType: Int, cashlessEnable: Int, cashlessAlimit: Int, array: Int, shake: Int, drive: String, comb: String) {
////        val command = 0x04.toByte()
////        val data = mapOf(
////            "TMODE" to tmode,
////            "TARGET" to target,
////            "FROST" to frost,
////            "TWORK" to twork,
////            "LEDL" to ledl,
////            "LPOWER" to lpower,
////            "GLHL" to glhl,
////            "GPOWER" to gpower,
////            "COIN" to mapOf("ENABLE" to coinEnable, "CHANGE" to coinChange),
////            "BILL" to mapOf("ENABLE" to billEnable, "CHANGE" to billChange, "ECROW" to billEcrow, "CHTYPE" to billChType),
////            "CASHLESS" to mapOf("ENABLE" to cashlessEnable, "ALIMIT" to cashlessAlimit),
////            "ARRAY" to array,
////            "SHAKE" to shake,
////            "DRIVE" to drive,
////            "COMB" to comb
////        )
//
//        viewModelScope.launch {
//            // Define your command, SN, and data
//            val command: Byte = 0x04.toByte() // Example command
//            // Example SN
//            val data: Map<String, Any> = mapOf("LEDL" to 1) // Example data
//
//// Call the generateCommandPacket function
//            generateCommandPacket(command, sn, data)
////            val commandPacket = generateCommandPacket(command, sn, data)
////            portConnectionDatasource.sendCommandVendingMachine(commandPacket)
//        }
//    }

    fun turnOnLed() {
        viewModelScope.launch {
            try {
//                val data: Map<String, Any> = mapOf("LEDL" to ledStatus) // Example data
                val jsonData = "{\"LEDL\": 1}"
                val byteArray = constructCommand(0x04, jsonData.toByteArray(), _sn.value.toByte())
//                logger.info("----->>>>>> ${baseRepository.byteArrayToHexString(byteArray)}")
                val byteArrays = byteArrayOf(
                    0x02.toByte(),      // Start default value
                    0x00.toByte(),          // Length
                    0x0E.toByte(),          // Length
                    0x04.toByte(),              // CMD
                    0x00.toByte(),              // ID default value
                    _sn.value.toByte(),              // SN
                    0x7B.toByte(),              // DATA[0,n]
                    0x22.toByte(),      // Ending default value
                    0x4C.toByte(), // CRC16
                    0x45.toByte(), // CRC16
                    0x44.toByte(), // CRC16
                    0x4C.toByte(), // CRC16
                    0x22.toByte(), // CRC16
                    0x3A.toByte(), // CRC16
                    0x20.toByte(), // CRC16
                    0x31.toByte(), // CRC16
                    0x03.toByte(), // CRC16
                    0xB5.toByte(), // CRC16
                    0x40.toByte(), // CRC16
                )
                portConnectionDatasource.sendCommandVendingMachine(byteArrays)
//                generateCommandPacket(command, _sn.value.toByte(), data)
                _sn.value += 1
            } catch (e: Exception) {
                logger.debug("error: ${e.message}")
            }
        }
    }

    // Function to turn on the LED
//    fun turnOnLed() {
//        viewModelScope.launch {
//            val byteArrays = byteArrayOf(
//                0x02.toByte(),      // Start default value
//                0x00.toByte(),          // Length
//                0x04.toByte(),          // Length
//                0x00.toByte(),              // CMD
//                0x00.toByte(),              // ID default value
//                0x71.toByte(),              // SN
//                0x00.toByte(),              // DATA[0,n]
//                0x03.toByte(),      // Ending default value
//                0x4C.toByte(), // CRC16
//                0xD0.toByte(), // CRC16
//            )
//
//            // Send the command packet
//            portConnectionDatasource.sendCommandVendingMachine(byteArrays)
//        }
//    }

    val crc16TabH = intArrayOf(
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1,
        0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1,
        0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1,
        0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1,
        0x81, 0x40, 0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
        0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0,
        0x80, 0x41, 0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
        0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40
    )
    val crc16TabL = intArrayOf(
        0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2,
        0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04, 0xCC, 0x0C,
        0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E, 0x0A, 0xCA, 0xCB, 0x0B,
        0xC9, 0x09, 0x08, 0xC8, 0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB,
        0xDA, 0x1A, 0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC,
        0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6, 0xD2, 0x12,
        0x13, 0xD3, 0x11, 0xD1, 0xD0, 0x10, 0xF0, 0x30, 0x31, 0xF1,
        0x33, 0xF3, 0xF2, 0x32, 0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35,
        0x34, 0xF4, 0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE,
        0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38, 0x28, 0xE8,
        0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA, 0xEE, 0x2E, 0x2F, 0xEF,
        0x2D, 0xED, 0xEC, 0x2C, 0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7,
        0xE6, 0x26, 0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0,
        0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62, 0x66, 0xA6,
        0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4, 0x6C, 0xAC, 0xAD, 0x6D,
        0xAF, 0x6F, 0x6E, 0xAE, 0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9,
        0xA8, 0x68, 0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA,
        0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C, 0xB4, 0x74,
        0x75, 0xB5, 0x77, 0xB7, 0xB6, 0x76, 0x72, 0xB2, 0xB3, 0x73,
        0xB1, 0x71, 0x70, 0xB0, 0x50, 0x90, 0x91, 0x51, 0x93, 0x53,
        0x52, 0x92, 0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54,
        0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E, 0x5A, 0x9A,
        0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98, 0x88, 0x48, 0x49, 0x89,
        0x4B, 0x8B, 0x8A, 0x4A, 0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D,
        0x4C, 0x8C, 0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86,
        0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40
    )

//    var crc16hi: UByte = 0xff.toUByte()
//    var crc16lo: UByte = 0xff.toUByte()

    // CRC Calculation
    fun calculateCrc16(data: ByteArray, length: Int): Int {
        var crcHi = 0xFF
        var crcLo = 0xFF
        for (i in 0 until length) {
            val index = (crcLo xor data[i].toInt()) and 0xFF
            crcLo = (crcHi xor crc16TabH[index]).toUByte().toInt()
            crcHi = crc16TabL[index].toUByte().toInt()
        }
        return (crcHi shl 8) or crcLo
    }

    // Helper function to construct command with CRC
    fun constructCommand(cmd: Byte, data: ByteArray, sn: Byte): ByteArray {
        val startByte: Byte = 0x02
        val endByte: Byte = 0x03
        val len = data.size + 3
        val command = ByteArray(len + 5)
        command[0] = startByte
        command[1] = (len shr 8).toByte()
        command[2] = (len and 0xFF).toByte()
        command[3] = cmd
        command[4] = 0x00
        command[5] = sn
        System.arraycopy(data, 0, command, 6, data.size)
        command[len + 4] = endByte
        val crc = calculateCrc16(command, len + 4)
        command[len + 3] = (crc shr 8).toByte()
        command[len + 2] = (crc and 0xFF).toByte()
        return command
    }

//    fun crc16Calculate(data: ByteArray) {
//        var iIndex: Int
//        data.forEach { byte ->
//            iIndex = (crc16lo.toInt() xor byte.toInt()) and 0xff
//            crc16lo = (crc16hi.toInt() xor crc16TabH[iIndex]).toUByte()
//            crc16hi = crc16TabL[iIndex].toUByte()
//        }
//    }

//    fun calculateCRC16(data: ByteArray): ByteArray {
//        val crc16TabH = intArrayOf(
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x00, 0xC1, 0x81, 0x40,
//            0x01, 0xC0, 0x80, 0x41, 0x01, 0xC0, 0x80, 0x41,
//            0x00, 0xC1, 0x81, 0x40
//        )
//
//        val crc16TabL = intArrayOf(
//            0x00, 0xC0, 0xC1, 0x01, 0xC3, 0x03, 0x02, 0xC2,
//            0xC6, 0x06, 0x07, 0xC7, 0x05, 0xC5, 0xC4, 0x04,
//            0xCC, 0x0C, 0x0D, 0xCD, 0x0F, 0xCF, 0xCE, 0x0E,
//            0x0A, 0xCA, 0xCB, 0x0B, 0xC9, 0x09, 0x08, 0xC8,
//            0xD8, 0x18, 0x19, 0xD9, 0x1B, 0xDB, 0xDA, 0x1A,
//            0x1E, 0xDE, 0xDF, 0x1F, 0xDD, 0x1D, 0x1C, 0xDC,
//            0x14, 0xD4, 0xD5, 0x15, 0xD7, 0x17, 0x16, 0xD6,
//            0xD2, 0x12, 0x13, 0xD3, 0x11, 0xD1, 0xD0, 0x10,
//            0xF0, 0x30, 0x31, 0xF1, 0x33, 0xF3, 0xF2, 0x32,
//            0x36, 0xF6, 0xF7, 0x37, 0xF5, 0x35, 0x34, 0xF4,
//            0x3C, 0xFC, 0xFD, 0x3D, 0xFF, 0x3F, 0x3E, 0xFE,
//            0xFA, 0x3A, 0x3B, 0xFB, 0x39, 0xF9, 0xF8, 0x38,
//            0x28, 0xE8, 0xE9, 0x29, 0xEB, 0x2B, 0x2A, 0xEA,
//            0xEE, 0x2E, 0x2F, 0xEF, 0x2D, 0xED, 0xEC, 0x2C,
//            0xE4, 0x24, 0x25, 0xE5, 0x27, 0xE7, 0xE6, 0x26,
//            0x22, 0xE2, 0xE3, 0x23, 0xE1, 0x21, 0x20, 0xE0,
//            0xA0, 0x60, 0x61, 0xA1, 0x63, 0xA3, 0xA2, 0x62,
//            0x66, 0xA6, 0xA7, 0x67, 0xA5, 0x65, 0x64, 0xA4,
//            0x6C, 0xAC, 0xAD, 0x6D, 0xAF, 0x6F, 0x6E, 0xAE,
//            0xAA, 0x6A, 0x6B, 0xAB, 0x69, 0xA9, 0xA8, 0x68,
//            0x78, 0xB8, 0xB9, 0x79, 0xBB, 0x7B, 0x7A, 0xBA,
//            0xBE, 0x7E, 0x7F, 0xBF, 0x7D, 0xBD, 0xBC, 0x7C,
//            0xB4, 0x74, 0x75, 0xB5, 0x77, 0xB7, 0xB6, 0x76,
//            0x72, 0xB2, 0xB3, 0x73, 0xB1, 0x71, 0x70, 0xB0,
//            0x50, 0x90, 0x91, 0x51, 0x93, 0x53, 0x52, 0x92,
//            0x96, 0x56, 0x57, 0x97, 0x55, 0x95, 0x94, 0x54,
//            0x9C, 0x5C, 0x5D, 0x9D, 0x5F, 0x9F, 0x9E, 0x5E,
//            0x5A, 0x9A, 0x9B, 0x5B, 0x99, 0x59, 0x58, 0x98,
//            0x88, 0x48, 0x49, 0x89, 0x4B, 0x8B, 0x8A, 0x4A,
//            0x4E, 0x8E, 0x8F, 0x4F, 0x8D, 0x4D, 0x4C, 0x8C,
//            0x44, 0x84, 0x85, 0x45, 0x87, 0x47, 0x46, 0x86,
//            0x82, 0x42, 0x43, 0x83, 0x41, 0x81, 0x80, 0x40
//        )
//        var crc16Hi: Int = 0xFF
//        var crc16Lo: Int = 0xFF
//
//        for (b in data) {
//            val iIndex = (crc16Lo xor b.toInt()) and 0xFF
//            crc16Lo = (crc16Hi xor crc16TabH[iIndex])
//            crc16Hi = crc16TabL[iIndex]
//        }
//
//        // Convert the CRC16 values to a byte array
//        return byteArrayOf(crc16Hi.toByte(), crc16Lo.toByte())
//    }

    // CRC16 calculation
//    fun calculateCRC16(data: ByteArray): ByteArray {
//        val crc16TabH = byteArrayOf(
//            0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(),
//            0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(),
//            0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte(), 0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(),
//            0x01.toByte(), 0xC0.toByte(), 0x80.toByte(), 0x41.toByte(), 0x00.toByte(), 0xC1.toByte(), 0x81.toByte(), 0x40.toByte()
//        )
//        val crc16TabL = byteArrayOf(
//            0x00.toByte(), 0xC0.toByte(), 0xC1.toByte(), 0x01.toByte(), 0xC3.toByte(), 0x03.toByte(), 0x02.toByte(), 0xC2.toByte(),
//            0xC6.toByte(), 0x06.toByte(), 0x07.toByte(), 0xC7.toByte(), 0x05.toByte(), 0xC5.toByte(), 0xC4.toByte(), 0x04.toByte(), 0xCC.toByte(), 0x0C.toByte(),
//            0x0D.toByte(), 0xCD.toByte(), 0x0F.toByte(), 0xCF.toByte(), 0xCE.toByte(), 0x0E.toByte(), 0x0A.toByte(), 0xCA.toByte(), 0xCB.toByte(), 0x0B.toByte(),
//            0xC9.toByte(), 0x09.toByte(), 0x08.toByte(), 0xC8.toByte(), 0xD8.toByte(), 0x18.toByte(), 0x19.toByte(), 0xD9.toByte(), 0x1B.toByte(), 0xDB.toByte(),
//            0xDA.toByte(), 0x1A.toByte(), 0x1E.toByte(), 0xDE.toByte(), 0xDF.toByte(), 0x1F.toByte(), 0xDD.toByte(), 0x1D.toByte(), 0x1C.toByte(), 0xDC.toByte(),
//            0x14.toByte(), 0xD4.toByte(), 0xD5.toByte(), 0x15.toByte(), 0xD7.toByte(), 0x17.toByte(), 0x16.toByte(), 0xD6.toByte(), 0xD2.toByte(), 0x12.toByte(),
//            0x13.toByte(), 0xD3.toByte(), 0x11.toByte(), 0xD1.toByte(), 0xD0.toByte(), 0x10.toByte(), 0xF0.toByte(), 0x30.toByte(), 0x31.toByte(), 0xF1.toByte(),
//            0x33.toByte(), 0xF3.toByte(), 0xF2.toByte(), 0x32.toByte(), 0x36.toByte(), 0xF6.toByte(), 0xF7.toByte(), 0x37.toByte(), 0xF5.toByte(), 0x35.toByte(),
//            0x34.toByte(), 0xF4.toByte(), 0x3C.toByte(), 0xFC.toByte(), 0xFD.toByte(), 0x3D.toByte(), 0xFF.toByte(), 0x3F.toByte(), 0x3E.toByte(), 0xFE.toByte(),
//            0xFA.toByte(), 0x3A.toByte(), 0x3B.toByte(), 0xFB.toByte(), 0x39.toByte(), 0xF9.toByte(), 0xF8.toByte(), 0x38.toByte(), 0x28.toByte(), 0xE8.toByte(),
//            0xE9.toByte(), 0x29.toByte(), 0xEB.toByte(), 0x2B.toByte(), 0x2A.toByte(), 0xEA.toByte(), 0xEE.toByte(), 0x2E.toByte(), 0x2F.toByte(), 0xEF.toByte(),
//            0x2D.toByte(), 0xED.toByte(), 0xEC.toByte(), 0x2C.toByte(), 0xE4.toByte(), 0x24.toByte(), 0x25.toByte(), 0xE5.toByte(), 0x27.toByte(), 0xE7.toByte(),
//            0xE6.toByte(), 0x26.toByte(), 0x22.toByte(), 0xE2.toByte(), 0xE3.toByte(), 0x23.toByte(), 0xE1.toByte(), 0x21.toByte(), 0x20.toByte(), 0xE0.toByte(),
//            0xA0.toByte(), 0x60.toByte(), 0x61.toByte(), 0xA1.toByte(), 0x63.toByte(), 0xA3.toByte(), 0xA2.toByte(), 0x62.toByte(), 0x66.toByte(), 0xA6.toByte(),
//            0xA7.toByte(), 0x67.toByte(), 0xA5.toByte(), 0x65.toByte(), 0x64.toByte(), 0xA4.toByte(), 0x6C.toByte(), 0xAC.toByte(), 0xAD.toByte(), 0x6D.toByte(),
//            0xAF.toByte(), 0x6F.toByte(), 0x6E.toByte(), 0xAE.toByte(), 0xAA.toByte(), 0x6A.toByte(), 0x6B.toByte(), 0xAB.toByte(), 0x69.toByte(), 0xA9.toByte(),
//            0xA8.toByte(), 0x68.toByte(), 0x78.toByte(), 0xB8.toByte(), 0xB9.toByte(), 0x79.toByte(), 0xBB.toByte(), 0x7B.toByte(), 0x7A.toByte(), 0xBA.toByte(),
//            0xBE.toByte(), 0x7E.toByte(), 0x7F.toByte(), 0xBF.toByte(), 0x7D.toByte(), 0xBD.toByte(), 0xBC.toByte(), 0x7C.toByte(), 0xB4.toByte(), 0x74.toByte(),
//            0x75.toByte(), 0xB5.toByte(), 0x77.toByte(), 0xB7.toByte(), 0xB6.toByte(), 0x76.toByte(), 0x72.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0x73.toByte(),
//            0xB1.toByte(), 0x71.toByte(), 0x70.toByte(), 0xB0.toByte(), 0x50.toByte(), 0x90.toByte(), 0x91.toByte(), 0x51.toByte(), 0x93.toByte(), 0x53.toByte(),
//            0x52.toByte(), 0x92.toByte(), 0x96.toByte(), 0x56.toByte(), 0x57.toByte(), 0x97.toByte(), 0x55.toByte(), 0x95.toByte(), 0x94.toByte(), 0x54.toByte(),
//            0x9C.toByte(), 0x5C.toByte(), 0x5D.toByte(), 0x9D.toByte(), 0x5F.toByte(), 0x9F.toByte(), 0x9E.toByte(), 0x5E.toByte(), 0x5A.toByte(), 0x9A.toByte(),
//            0x9B.toByte(), 0x5B.toByte(), 0x99.toByte(), 0x59.toByte(), 0x58.toByte(), 0x98.toByte(), 0x88.toByte(), 0x48.toByte(), 0x49.toByte(), 0x89.toByte(),
//            0x4B.toByte(), 0x8B.toByte(), 0x8A.toByte(), 0x4A.toByte(), 0x4E.toByte(), 0x8E.toByte(), 0x8F.toByte(), 0x4F.toByte(), 0x8D.toByte(), 0x4D.toByte(),
//            0x4C.toByte(), 0x8C.toByte(), 0x44.toByte(), 0x84.toByte(), 0x85.toByte(), 0x45.toByte(), 0x87.toByte(), 0x47.toByte(), 0x46.toByte(), 0x86.toByte(),
//            0x82.toByte(), 0x42.toByte(), 0x43.toByte(), 0x83.toByte(), 0x41.toByte(), 0x81.toByte(), 0x80.toByte(), 0x40.toByte()
//        )
//
//        var crcHi: Byte = 0xFF.toByte()
//        var crcLo: Byte = 0xFF.toByte()
//
//        for (byte in data) {
//            val index = (crcLo.toInt() xor byte.toInt()) and 0xFF // Calculate lookup table index
//            crcLo = (crcHi.toInt() xor crc16TabH[index].toInt()).toByte() // Update crcLo
//            crcHi = crc16TabL[index] // Update crcHi
//        }
//
//        return byteArrayOf(crcLo, crcHi) // Return CRC bytes
//    }

    fun check1() {
        logger.debug("check1")
        viewModelScope.launch {
            portConnectionDatasource.sendCommandVendingMachine(byteArrays.vmTurnOnLight)
        }
    }
    fun check2() {
        logger.debug("check1")
        viewModelScope.launch {
            portConnectionDatasource.sendCommandVendingMachine(byteArrays.vmTurnOffLight)
        }
    }

//    fun buildLedCommand(): ByteArray {
//        val startByte: Byte = 0x02
//        val cmd: Byte = 0x05
//        val id: Byte = 0x00
//        val sn: Byte = generateSn()  // Implement this to generate SN (0-255)
//        val dataJson = """{"LEDS":1}"""  // JSON data to turn on the LED light
//        val dataBytes = dataJson.toByteArray(Charsets.UTF_8)
//        val len = intTo2ByteArray(dataBytes.size + 3)  // 2 bytes length
//        val endByte: Byte = 0x03
//
//        // Concatenate all parts to prepare for CRC16 calculation
//        val messageWithoutCrc = byteArrayOf(startByte) + len + byteArrayOf(cmd, id, sn) + dataBytes + byteArrayOf(endByte)
//
//        // Calculate CRC16 for the message
//        val crc = calculateCrc16(messageWithoutCrc)
//
//        // Build the final message including CRC16
//        return messageWithoutCrc + crc
//    }

    // Utility function to convert int to 2-byte array
//    fun intTo2ByteArray(value: Int): ByteArray {
//        return byteArrayOf((value shr 8).toByte(), (value and 0xFF).toByte())
//    }

    // Implement CRC16 calculation based on the provided algorithm
//    fun calculateCrc16(data: ByteArray): ByteArray {
//        var crc = 0xFFFF
//        for (byte in data) {
//            crc = crc xor (byte.toInt() and 0xFF)
//            for (i in 0 until 8) {
//                if ((crc and 0x0001) != 0) {
//                    crc = (crc shr 1) xor 0xA001
//                } else {
//                    crc = crc shr 1
//                }
//            }
//        }
//        return byteArrayOf((crc and 0xFF).toByte(), ((crc shr 8) and 0xFF).toByte())
//    }

    // Implement SN generation (0-255 loop)
//    var snCounter = 0
//    fun generateSn(): Byte {
//        val sn = snCounter.toByte()
//        snCounter = (snCounter + 1) % 256
//        return sn
//    }

}