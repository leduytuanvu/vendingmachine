package com.leduytuanvu.vendingmachine.features.home.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.components.LoadingDialogComponent
import com.leduytuanvu.vendingmachine.features.home.presentation.view_model.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.view_state.HomeViewState
import com.leduytuanvu.vendingmachine.features.setting.presentation.components.ButtonComponent
import com.leduytuanvu.vendingmachine.features.setting.presentation.view_state.SettingViewState
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_model.SplashViewModel

@Composable
internal fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(HomeViewState())
}

@Composable
fun HomeContent(state: HomeViewState) {
    LoadingDialogComponent(isLoading = state.isLoading)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text(text = "HOME")
            }
        )
    }
}