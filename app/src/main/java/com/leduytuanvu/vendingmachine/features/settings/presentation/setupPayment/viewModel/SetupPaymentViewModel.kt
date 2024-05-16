package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewState.SetupPaymentViewState
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        startCollectingData()
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
                    _state.update { it.copy(cashBoxData = data) }
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
}