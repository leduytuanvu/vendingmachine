package com.combros.vendingmachine.features.splash.presentation.splash.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.features.splash.presentation.splash.viewModel.SplashViewModel
import com.combros.vendingmachine.features.splash.presentation.splash.viewState.SplashViewState

@Composable
internal fun SplashScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.handleInit(navController = navController)
    }
    SplashContent(state = state)
}

@Composable
fun SplashContent(state: SplashViewState) {
    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFCB1A17))
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = { }
        )
    }
}