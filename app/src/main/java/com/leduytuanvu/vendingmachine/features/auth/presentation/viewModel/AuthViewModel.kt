package com.leduytuanvu.vendingmachine.features.auth.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.auth.presentation.viewState.AuthViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor (
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthViewState())
    val state = _state.asStateFlow()

//    fun login(vendCode: String, loginRequest: LoginRequest) {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//                authRepository.login(vendCode, loginRequest)
//            } catch (e: Exception) {
//                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "login()")
//            } finally {
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }
}