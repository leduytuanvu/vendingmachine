package com.leduytuanvu.vendingmachine.features.splash.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leduytuanvu.vendingmachine.core.errors.CustomError
import com.leduytuanvu.vendingmachine.core.room.Repository
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.splash.domain.repository.SplashRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor (
    private val splashRepository: SplashRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SplashViewState())
    val state = _state.asStateFlow()

    fun initCheckVendCodeExists() {
        checkVendCodeExists()
        val repository: Repository = Graph.repository
    }

    fun navigateTo(navigateTo: String) {
        _state.value = _state.value.copy(navigateTo = navigateTo)
    }

    private fun checkVendCodeExists() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(3000)
                val isVendCodeExists = splashRepository.checkVendCodeExists()
                if(isVendCodeExists) {
                    _state.update { it.copy(navigateTo = Screens.HomeScreenRoute.route) }
                } else {
                    _state.update { it.copy(navigateTo = Screens.InitSettingScreenRoute.route) }
                }
            } catch (e: Exception) {
                val customError = CustomError.GeneralException(
                    message = e.message,
                    function = "checkVendCodeExists",
                    localizedMessage = e.localizedMessage,
                    cause = e.cause.toString(),
                )
                _state.update { it.copy(error = customError) }
                sendEvent(Event.Toast(e.message!!))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}