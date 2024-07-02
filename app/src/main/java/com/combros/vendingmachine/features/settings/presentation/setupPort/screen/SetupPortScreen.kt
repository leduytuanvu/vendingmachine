package com.combros.vendingmachine.features.settings.presentation.setupPort.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.itemsPort
import com.combros.vendingmachine.core.util.itemsTypeVendingMachine
import com.combros.vendingmachine.features.settings.presentation.setupPort.viewModel.SetupPortViewModel
import com.combros.vendingmachine.features.settings.presentation.setupPort.viewState.SetupPortViewState
import kotlinx.coroutines.delay

@Composable
internal fun SetupPortScreen(
    navController: NavHostController,
    viewModel: SetupPortViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadInitSetup()
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.SetupPortScreenRoute.route) {
                        inclusive = true
                    }
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
        SetupPortContent(
            state = state,
            viewModel = viewModel,
            navController = navController,
            onClick = { lastInteractionTime = System.currentTimeMillis() }
        )
    }
//    SetupPortContent(
//        state = state,
//        viewModel = viewModel,
//        navController = navController
//    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupPortContent(
    state: SetupPortViewState,
    viewModel: SetupPortViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
) {
    var selectedItemTypeVendingMachine by remember {
        mutableStateOf(
            AnnotatedString("")
        )
    }
    var selectedItemPortCashBox by remember {
        mutableStateOf(
            AnnotatedString("")
        )
    }
    var selectedItemPortVendingMachine by remember {
        mutableStateOf(
            AnnotatedString("")
        )
    }

    LaunchedEffect(state.initSetup) {
        selectedItemTypeVendingMachine = AnnotatedString(state.initSetup?.typeVendingMachine ?: "")
        selectedItemPortCashBox = AnnotatedString(state.initSetup?.portCashBox ?: "")
        selectedItemPortVendingMachine = AnnotatedString(state.initSetup?.portVendingMachine ?: "")
    }

    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                onClick()
            }
        )
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )
                },
            content = {
                CustomButtonComposable(
                    title = "BACK",
                    wrap = true,
                    height = 70.dp,
                    fontSize = 20.sp,
                    cornerRadius = 4.dp,
                    paddingBottom = 30.dp,
                    fontWeight = FontWeight.Bold,
                ) {
                    navController.popBackStack()
                }
                TitleAndDropdownComposable(
                    title = "Choose the type of vending machine",
                    items = itemsTypeVendingMachine,
                    selectedItem = selectedItemTypeVendingMachine,
                ) {
                    selectedItemTypeVendingMachine = it
                    onClick()
                }
                TitleAndDropdownComposable(
                    title = "Select the port to connect to vending machine",
                    items = itemsPort,
                    selectedItem = selectedItemPortVendingMachine,
                ) {
                    selectedItemPortVendingMachine = it
                    onClick()
                }
                TitleAndDropdownComposable(
                    title = "Select the port to connect to cash box",
                    items = itemsPort,
                    selectedItem = selectedItemPortCashBox,
                ) {
                    selectedItemPortCashBox = it
                    onClick()
                }
                Spacer(modifier = Modifier.weight(1f))
                CustomButtonComposable(
                    title = "SAVE SETUP PORT",
                    titleAlignment = TextAlign.Center,
                    paddingBottom = 20.dp,
                    cornerRadius = 4.dp,
                    height = 70.dp,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ) {
                    viewModel.saveSetupPort(
                        typeVendingMachine = selectedItemTypeVendingMachine.toString(),
                        portCashBox = selectedItemPortCashBox.toString(),
                        portVendingMachine = selectedItemPortVendingMachine.toString(),
                    )
                    onClick()
                }
            }
        )
    }
}