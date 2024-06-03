package com.leduytuanvu.vendingmachine.features.auth.presentation.viewModel

import android.content.Context
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.auth.presentation.viewState.AuthViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val authRepository: AuthRepository,
    private val baseRepository: BaseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthViewState())
    val state = _state.asStateFlow()

    fun login(
        context: Context,
        username: String,
        password: String,
        navController: NavHostController,
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                if(baseRepository.isHaveNetwork(context)) {
                    val loginRequestHaveNetwork = LoginRequest(
                        username,
                        password,
                    )
                    val response = authRepository.login(initSetup.vendCode, loginRequestHaveNetwork)
                    if(response.accessToken.isNotEmpty()) {
                        baseRepository.addNewAuthyLogToLocal(
                            machineCode = initSetup.vendCode,
                            authyType = "login",
                            username = loginRequestHaveNetwork.username,
                        )
                        navController.navigate(Screens.SettingScreenRoute.route) {
                            popUpTo(Screens.LoginScreenRoute.route) {
                                inclusive = true
                            }
                            popUpTo(Screens.HomeScreenRoute
                                .route) {
                                inclusive = true
                            }
                        }
                    } else {
                        sendEvent(Event.Toast("Username, password, or vending machine code fail!"))
                    }

                } else {
                    val dataPassword = Base64.decode(initSetup.password, Base64.DEFAULT)
                    val realPassword = String(dataPassword, Charsets.UTF_8).substringBefore("567890VENDINGMACHINE")
                    if(username==initSetup.username && password==realPassword) {
                        baseRepository.addNewAuthyLogToLocal(
                            machineCode = initSetup.vendCode,
                            authyType = "login",
                            username = initSetup.username,
                        )
                        navController.popBackStack()
                        navController.popBackStack()
                        navController.navigate(Screens.SettingScreenRoute.route)
                    } else {
                        sendEvent(Event.Toast("Username, password, or vending machine code fail!"))
                    }
                }
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Logger.debug("error: ${e.message}")
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "login fail in AuthViewModel/login(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}