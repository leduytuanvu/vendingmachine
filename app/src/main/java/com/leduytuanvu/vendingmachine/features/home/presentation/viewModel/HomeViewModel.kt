package com.leduytuanvu.vendingmachine.features.home.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor (
//    private val splashRepository: SplashRepository
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()


}