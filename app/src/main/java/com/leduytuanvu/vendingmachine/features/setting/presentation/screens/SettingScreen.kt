package com.leduytuanvu.vendingmachine.features.setting.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.components.LoadingDialogComponent
import com.leduytuanvu.vendingmachine.features.setting.presentation.components.ButtonComponent
import com.leduytuanvu.vendingmachine.features.setting.presentation.view_state.SettingViewState

@Composable
internal fun SettingScreen(navController: NavHostController) {
    SettingContent(SettingViewState())
}

@Composable
fun SettingContent(state: SettingViewState) {
    LoadingDialogComponent(isLoading = state.isLoading)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text(text = "SETTING")
                ButtonComponent(
                    title = "HOME",
                ) {
                    Log.d("tuanvulog", "set up port")
                }

                ButtonComponent(
                    title = "SET UP PORT",
                ) {
                    Log.d("tuanvulog", "set up port")
                }

                ButtonComponent(
                    title = "SET UP PRODUCT",
                ) {
                    Log.d("tuanvulog", "set up port")
                }

                ButtonComponent(
                    title = "SET UP SLOT",
                ) {
                    Log.d("tuanvulog", "set up port")
                }

                ButtonComponent(
                    title = "SET UP SYSTEM",
                ) {
                    Log.d("tuanvulog", "set up port")
                }

                ButtonComponent(
                    title = "SET UP PAYMENT",
                ) {
                    Log.d("tuanvulog", "set up port")
                }
            }
        )
    }
}
