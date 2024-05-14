package com.leduytuanvu.vendingmachine.features.splash.presentation.splash.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogAuthy
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.splash.viewState.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor (
    private val baseRepository: BaseRepository,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashViewState())
    val state = _state.asStateFlow()

    // DONE
    fun handleInit(navController: NavHostController) {
        logger.info("handleInit")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val isFileInitSetupExists = baseRepository.isFileInitSetupExists()
                navController.popBackStack()
                if (isFileInitSetupExists) {
                    val initSetup: InitSetup? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )
                    if(initSetup != null && initSetup.portCashBox.isNotEmpty() && initSetup.portVendingMachine.isNotEmpty()) {
                        if (portConnectionDataSource.openPortCashBox(initSetup.portCashBox) == -1) {
                            logger.info("Open port cash box is error!")
                        } else {
                            logger.info("Open port cash box success")
                            portConnectionDataSource.startReadingCashBox()
                        }
                        if (portConnectionDataSource.openPortVendingMachine(initSetup.portVendingMachine) == -1) {
                            logger.info("Open port vending machine is error!")
                        } else {
                            logger.info("Open port vending machine success")
                            portConnectionDataSource.startReadingVendingMachine()
                        }
                        navController.navigate(Screens.SettingScreenRoute.route)
                    } else {
                        navController.navigate(Screens.InitSettingScreenRoute.route)
                    }
                } else {
                    navController.navigate(Screens.InitSettingScreenRoute.route)
                }
            } catch (e: Exception) {
                val logError = LogError(
                    machineCode = "",
                    errorType = "application",
                    errorContent = "handle init fail: ${e.message}",
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