package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewModel

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
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFilePaymentMethod
import com.leduytuanvu.vendingmachine.core.util.pathFolderImagePayment
import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewState.SetupPaymentViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SetupPaymentViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupPaymentViewState())
    val state = _state.asStateFlow()

    init {
        loadInitData()
        startCollectingData()
    }

    fun loadInitData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load init data fail in HomeViewModel/loadInitData(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveDefaultPromotion(defaultPromotion: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.initPromotion = defaultPromotion
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load init data fail in HomeViewModel/loadInitData(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshCurrentCash() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.currentCash = 0
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "refresh current cash fail in HomeViewModel/refreshCurrentCash(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun startCollectingData() {
        viewModelScope.launch {
            launch {
                portConnectionDatasource.dataFromVendingMachine.collect { data ->
                    _state.update { it.copy(vendingMachineData = data) }
                }
            }
            launch {
                portConnectionDatasource.dataFromCashBox.collect { data ->
//                    _state.update { it.copy(cashBoxData = data) }
                }
            }
        }
    }

    fun turnOnLight() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOnLight)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun pollStatus() {
        logger.debug("pollStatus")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun turnOffLight() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOffLight)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun checkDropSensor() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmCheckDropSensor)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getListMethodPayment() {
        logger.debug("getListMethodPayment")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listPaymentMethod = settingsRepository.getListPaymentMethodFromServer()
                for (item in listPaymentMethod) {
                    logger.debug(item.toString())
                }
                baseRepository.writeDataToLocal(data = listPaymentMethod, path = pathFilePaymentMethod)
                _state.update { it.copy(
                    listPaymentMethod = listPaymentMethod,
                    isLoading = false,
                ) }
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load list payment method fail in SetUpPaymentViewModel/getListMethodPayment(): ${e.message}",
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

    fun downloadListMethodPayment() {
        logger.debug("getListMethodPayment")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listPaymentMethod = settingsRepository.getListPaymentMethodFromServer()
                    if (!baseRepository.isFolderExists(pathFolderImagePayment)) {
                        baseRepository.createFolder(pathFolderImagePayment)
                    }
                    for (item in listPaymentMethod) {
                        if(item.imageUrl!!.isNotEmpty()) {
                            var notHaveError = true
                            for (i in 1..3) {
                                try {
                                    val request = ImageRequest.Builder(context = context)
                                        .data(item.imageUrl)
                                        .build()
                                    val result = withContext(Dispatchers.IO) {
                                        Coil.imageLoader(context).execute(request).drawable
                                    }
                                    if (result != null) {
                                        val file =
                                            File(pathFolderImagePayment, "${item.methodName}.png")
                                        withContext(Dispatchers.IO) {
                                            file.outputStream().use { outputStream ->
                                                result.toBitmap().compress(
                                                    Bitmap.CompressFormat.PNG,
                                                    1,
                                                    outputStream
                                                )
                                            }
                                        }
                                    }
                                    logger.debug("download ${item.imageUrl} success")
                                } catch (e: Exception) {
                                    notHaveError = false
                                    logger.debug("${e.message}")
                                } finally {
                                    if (notHaveError) break
                                }
                            }
                        }
                    }
                    baseRepository.writeDataToLocal(data = listPaymentMethod, path = pathFilePaymentMethod)
                    _state.update { it.copy(
                        listPaymentMethod = listPaymentMethod,
                        isLoading = false,
                    ) }
                    sendEvent(Event.Toast("SUCCESS"))
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load list payment method fail in SetUpPaymentViewModel/getListMethodPayment(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}