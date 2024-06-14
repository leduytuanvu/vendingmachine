package com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSyncOrder
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogsLocal
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileLogServer
import com.leduytuanvu.vendingmachine.core.util.pathFileSyncOrder
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTime
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
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

    fun loadInittransaction() {
        logger.debug("loadInittransaction")
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
                val countTransactionByCash = 0
                val amountTransactionByCash = 0
//                for(item in listSyncOrder) {
//                    item.
//                }
                _state.update { it.copy(isLoading = false, initSetup = initSetup) }
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