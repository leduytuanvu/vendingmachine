package com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogFill
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServerLocal
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileLogServer
import com.leduytuanvu.vendingmachine.core.util.pathFileProductDetail
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTime
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val settingsRepository: SettingsRepository,
    private val baseRepository: BaseRepository,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
        viewModelScope.launch {
            if (baseRepository.isHaveNetwork(context)) {
                _state.update { it.copy(
                    isConfirm = true,
                    titleDialogConfirm = mess,
                    slot = slot,
                    nameFunction = nameFunction,
                ) }
            } else {
                showDialogWarning("Not have internet, please connect with internet!")
            }
        }
    }

    fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                isWarning = true,
                titleDialogWarning = mess,
            ) }
        }
    }

    fun getInitInformationOfMachine() {
        logger.debug("getInitInformationOfMachine")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(informationOfMachine = informationOfMachine) }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
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

    fun getInformationOfMachine() {
        logger.debug("getInformationOfMachine")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val informationOfMachine = settingsRepository.getInformationOfMachine()
                _state.update { it.copy(informationOfMachine = informationOfMachine) }
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

    fun getSerialSimId() {
        logger.debug("getSerialSimId")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val serialSimId = settingsRepository.getSerialSimId()
                _state.update { it.copy(serialSimId = serialSimId) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get serial sim fail: ${e.message}",
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

    fun getAllLogServerLocal() {
        logger.debug("getAllLogServerLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listLogServerLocal: ArrayList<LogServerLocal> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogServerLocal>>() {}.type,
                    path = pathFileLogServer
                )!!
                listLogServerLocal.sortByDescending { it.eventTime.toDateTime() }
                _state.update { it.copy(listLogServerLocal = listLogServerLocal) }
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
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

    fun updateHighestTempWarningInLocal(highestTempWarning: String) {
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

    fun check() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDataSource.sendCommandVendingMachine(ByteArrays().vmCheckDropSensor)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}