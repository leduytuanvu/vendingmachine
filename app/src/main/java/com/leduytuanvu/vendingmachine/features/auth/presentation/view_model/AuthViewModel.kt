package com.leduytuanvu.vendingmachine.features.auth.presentation.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.core.util.exceptionHandling
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.auth.presentation.view_state.AuthViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val authRepository: AuthRepository,
    private val localStorageDatasource: LocalStorageDatasource,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthViewState())
    val state = _state.asStateFlow()

    fun login(vendCode: String, loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                authRepository.login(vendCode, loginRequest)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "login()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}