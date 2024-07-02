package com.combros.vendingmachine.features.settings.presentation.settings.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleTextComposable
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.combros.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import kotlinx.coroutines.delay

@Composable
internal fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadInitData()
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.SettingScreenRoute.route) {
                        inclusive = true
                    }
                }
                return@LaunchedEffect
            }
            delay(1000)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        lastInteractionTime = System.currentTimeMillis()
                    }
                )
            }
    ) {
        SettingsContent(
            state = state,
            viewModel = viewModel,
            navController = navController,
        )
    }
//    SettingsContent(
//        state = state,
//        viewModel = viewModel,
//        navController = navController,
//    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsContent(
    state: SettingsViewState,
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = { viewModel.hideDialogConfirm() },
        onClickConfirm = { viewModel.deactivateMachine(navController = navController) },
    )
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = {
                TitleTextComposable(title = "GENERAL SETTINGS")
                ButtonSettingsComposable("HOME", function = {
                    navController.navigate(Screens.HomeScreenRoute.route)
                }, )
//                ButtonSettingsComposable("HOME ANDROID", function = {
//                    throw Exception("Deliberate crash for testing purposes")
//                })
                if(state.initSetup!=null) {
                    if(state.initSetup.role == "admin") {
                        ButtonSettingsComposable("SET UP PORT", function = {
                            navController.navigate(Screens.SetupPortScreenRoute.route)
                        })
                    }
                }
                ButtonSettingsComposable("SET UP PRODUCT", function = {
                    navController.navigate(Screens.SetupProductScreenRoute.route)
                })
                ButtonSettingsComposable("SET UP SLOT", function = {
                    navController.navigate(Screens.SetupSlotScreenRoute.route)
                })
                if(state.initSetup!=null) {
                    if(state.initSetup.role == "admin") {
                        ButtonSettingsComposable("SET UP SYSTEM", function = {
                            navController.navigate(Screens.SetupSystemScreenRoute.route)
                        })
                    }
                }
                if(state.initSetup!=null) {
                    if(state.initSetup.role == "admin") {
                        ButtonSettingsComposable("SET UP PAYMENT", function = {
                            navController.navigate(Screens.SetupPaymentScreenRoute.route)
                        })
                    }
                }
                ButtonSettingsComposable("VIEW TRANSACTIONS IN THE LAST 7 DAYS", function = {
                    navController.navigate(Screens.TransactionScreenRoute.route)
                })
                ButtonSettingsComposable("VIEW LOG", function = {
                    navController.navigate(Screens.ViewLogScreenRoute.route)
                })
                if(state.initSetup!=null) {
                    if(state.initSetup.role == "admin") {
                        ButtonSettingsComposable("RESET FACTORY", function = {
                            viewModel.showDialogConfirm("Are you sure you want to reset factory?")
                        })
                    }
                }
            }
        )
    }
}

@Composable
fun ButtonSettingsComposable(title: String, function: () -> Unit) {
    CustomButtonComposable(
        title = title,
        titleAlignment = TextAlign.Start,
        paddingBottom = 14.dp,
        cornerRadius = 4.dp,
        height = 70.dp,
        function = function,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
}
