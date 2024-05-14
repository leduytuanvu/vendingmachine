package com.leduytuanvu.vendingmachine.features.settings.presentation.settings.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleTextComposable
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState

@Composable
internal fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsContent(
        state = state,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsContent(
    state: SettingsViewState,
    navController: NavHostController,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = {
                TitleTextComposable(title = "GENERAL SETTINGS")
                ButtonSettingsComposable("HOME", function = {
                    navController.navigate(Screens.HomeScreenRoute.route)
                }, )
                ButtonSettingsComposable("SET UP PORT", function = {
                    navController.navigate(Screens.SetupPortScreenRoute.route)
                })
                ButtonSettingsComposable("SET UP PRODUCT", function = {
                    navController.navigate(Screens.SetupProductScreenRoute.route)
                })
                ButtonSettingsComposable("SET UP SLOT", function = {
                    navController.navigate(Screens.SetupSlotScreenRoute.route)
                })
                ButtonSettingsComposable("SET UP SYSTEM", function = {
                    navController.navigate(Screens.SetupSystemScreenRoute.route)
                })
                ButtonSettingsComposable("SET UP PAYMENT", function = {
                    navController.navigate(Screens.SetupPaymentScreenRoute.route)
                })
                ButtonSettingsComposable("VIEW LOG", function = {
                    navController.navigate(Screens.ViewLogScreenRoute.route)
                })
            }
        )
    }
}

@Composable
fun ButtonSettingsComposable(title: String, function: () -> Unit) {
    CustomButtonComposable(
        title = title,
        titleAlignment = TextAlign.Start,
        paddingBottom = 10.dp,
        cornerRadius = 4.dp,
        height = 65.dp,
        function = function,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
}
