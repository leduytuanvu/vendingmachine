package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewState.SetupSlotViewState
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SetupSystemViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupSystemViewState())
    val state = _state.asStateFlow()

    init {
        getInitSetupFromLocal()
        getSerialSimId()
        getInitInformationOfMachine()
    }

    fun getInitInformationOfMachine() {
        logger.debug("getInitInformationOfMachine")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(informationOfMachine = informationOfMachine) }
                } else {
//                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get init information machine from server fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun check() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOnLight)
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbEnable123456789)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
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

    fun updateInchingModeInLocal(inchingMode: String) {
        logger.debug("updateInchingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.inchingMode = inchingMode
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
                initSetup.timeToJumpToAdsScreen = timeToJumpToAdsScreen
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

    fun updateGlassHeatingModeInLocal(glassHeatingMode: String) {
        logger.debug("updateGlassHeatingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.glassHeatingMode = glassHeatingMode
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

    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
        viewModelScope.launch {
//            if (baseRepository.isHaveNetwork(context)) {
//                _state.update { it.copy(
//                    isConfirm = true,
//                    titleDialogConfirm = mess,
//                    slot = slot,
//                    nameFunction = nameFunction,
//                ) }
//            } else {
//                showDialogWarning("Not have internet, please connect with internet!")
//            }
        }
    }

    fun updateTemperatureInLocal(temperature: String) {
        logger.debug("updateTemperatureInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.temperature = temperature
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
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getInformationOfMachine() {
        logger.debug("getInformationOfMachine")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    delay(500)
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(informationOfMachine = informationOfMachine) }
                } else {
//                    showDialog
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get information machine from server fail: ${e.message}",
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