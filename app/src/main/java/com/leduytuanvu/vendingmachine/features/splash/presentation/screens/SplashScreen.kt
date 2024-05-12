package com.leduytuanvu.vendingmachine.features.splash.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_model.SplashViewModel
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState

@Composable
internal fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DisposableEffect(Unit) {
        viewModel.checkFileInitSetupExists(navController = navController)
        onDispose { }
    }
    SplashContent(state = state)
}

@Composable
fun SplashContent(
    state: SplashViewState
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = { }
        )
    }
}