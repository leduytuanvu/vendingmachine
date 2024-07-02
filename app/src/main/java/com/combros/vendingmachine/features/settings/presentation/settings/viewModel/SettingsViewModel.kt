package com.combros.vendingmachine.features.settings.presentation.settings.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.model.LogSyncOrder
import com.combros.vendingmachine.common.base.domain.model.LogsLocal
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository
import com.combros.vendingmachine.core.util.Event
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.core.util.pathFileLogServer
import com.combros.vendingmachine.core.util.pathFileSyncOrder
import com.combros.vendingmachine.core.util.sendEvent
import com.combros.vendingmachine.core.util.toDateTime
import com.combros.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.combros.vendingmachine.features.auth.domain.repository.AuthRepository
import com.combros.vendingmachine.features.settings.domain.model.Slot
import com.combros.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.combros.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    fun loadInitData() {
        logger.debug("loadInitSetup")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                _state.update { it.copy(isLoading = false, initSetup = initSetup) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadInitTransaction() {
        logger.debug("loadInitTransaction")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val listSyncOrder: ArrayList<LogSyncOrder> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                    path = pathFileSyncOrder
                )!!
                var countTransactionByCash = 0
                var amountTransactionByCash = 0
                var countTransactionByOnline = 0
                var amountTransactionByOnline = 0
                if(listSyncOrder.isNotEmpty()) {
                    if(initSetup.timeClosingSession.isEmpty()) {
                        for(item in listSyncOrder) {
                            if(item.paymentMethodId=="cash") {
                                countTransactionByCash+=1
                                for(itemTmp in item.productDetails) {
                                    if(itemTmp.deliveryStatus == "success") {
                                        amountTransactionByCash+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                    }
                                }
                            } else {
                                countTransactionByOnline+=1
                                for(itemTmp in item.productDetails) {
                                    if(itemTmp.deliveryStatus == "success") {
                                        amountTransactionByOnline+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                    }
                                }
                            }
                        }
                    } else {

                    }
                }
                _state.update { it.copy(
                    isLoading = false,
                    initSetup = initSetup,
                    countTransactionByCash = countTransactionByCash,
                    amountTransactionByCash = amountTransactionByCash,
                    countTransactionByOnline = countTransactionByOnline,
                    amountTransactionByOnline = amountTransactionByOnline,
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

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

    fun makeAppCrash() {
        viewModelScope.launch {
            throw RuntimeException("Deliberate crash for testing purposes")
        }
    }


    fun showDialogConfirm(mess: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    titleDialogConfirm = mess,
                    isConfirm = true,
                )
            }
        }
    }

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

    fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                isWarning = true,
                titleDialogWarning = mess,
            ) }
        }
    }

    fun deactivateMachine(navController: NavHostController) {
        logger.debug("deactivateMachine")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, isConfirm = false) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val deactivateRequest = ActivateTheMachineRequest(
                    machineCode = initSetup.vendCode,
                    androidId = initSetup.androidId,
                )
                val response = authRepository.deactivateTheMachine(deactivateRequest)
                if(response.code==200) {
                    sendEvent(Event.Toast("Deactivated successfully"))
                    baseRepository.deleteFile(pathFileInitSetup)
                    baseRepository.addNewSetupLogToLocal(
                        machineCode = initSetup.vendCode,
                        operationContent = "reset factory",
                        operationType = "settings",
                        username = initSetup.username,
                    )
                    navController.navigate(Screens.SplashScreenRoute.route) {
                        popUpTo(Screens.SettingScreenRoute.route) {
                            inclusive = true
                        }
                    }
                } else {
                    sendEvent(Event.Toast(response.message))
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
                val listLogServerLocal: ArrayList<LogsLocal> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogsLocal>>() {}.type,
                    path = pathFileLogServer
                )!!
                listLogServerLocal.sortByDescending { it.eventTime.toDateTime() }
                _state.update { it.copy(listLogServerLocal = listLogServerLocal) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "load log server local fail in SettingsViewModel/getAllLogServerLocal(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}