package com.leduytuanvu.vendingmachine.features.splash.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleTextComposable
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.common.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleAndEditTextComposable
import com.leduytuanvu.vendingmachine.core.util.itemsPort
import com.leduytuanvu.vendingmachine.core.util.itemsTypeVendingMachine
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_model.SplashViewModel
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState

@Composable
internal fun InitSettingScreen(
    navController: NavHostController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InitSettingContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InitSettingContent(
    state: SplashViewState,
    viewModel: SplashViewModel,
    navController: NavHostController,
) {
    var inputVendingMachineCode by remember { mutableStateOf("") }
    var inputUsername by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var selectedItemTypeVendingMachine by remember { mutableStateOf(AnnotatedString("TCN")) }
    var selectedItemPortCashBox by remember { mutableStateOf(AnnotatedString("ttyS2")) }
    var selectedItemPortVendingMachine by remember { mutableStateOf(AnnotatedString("ttyS1")) }

    LoadingDialogComposable(isLoading = state.isLoading)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            content = {
                TitleTextComposable(title = "INIT SETUP FOR VENDING MACHINE")

                TitleAndEditTextComposable(title = "Enter vending machine code") {
                    inputVendingMachineCode = it
                }

                TitleAndEditTextComposable(title = "Enter username") {
                    inputUsername = it
                }

                TitleAndEditTextComposable(title = "Enter password") {
                    inputPassword = it
                }

                TitleAndDropdownComposable(
                    title = "Choose the type of vending machine",
                    items = itemsTypeVendingMachine,
                    selectedItem = selectedItemTypeVendingMachine,
                ) {
                    selectedItemTypeVendingMachine = it
                }

                TitleAndDropdownComposable(
                    title = "Select the port to connect to vending machine",
                    items = itemsPort,
                    selectedItem = selectedItemPortVendingMachine,
                ) {
                    selectedItemPortVendingMachine = it
                }

                TitleAndDropdownComposable(
                    title = "Select the port to connect to cash box",
                    items = itemsPort,
                    selectedItem = selectedItemPortCashBox,
                ) {
                    selectedItemPortCashBox = it
                }

                Spacer(modifier = Modifier.weight(1f))

                CustomButtonComposable(
                    title = "SAVE INIT SETUP",
                    titleAlignment = TextAlign.Center,
                    cornerRadius = 4.dp,
                    height = 65.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ) {
                    val loginRequest = LoginRequest(inputUsername, inputPassword)
                    viewModel.writeInitSetupToLocal(
                        inputVendingMachineCode,
                        selectedItemPortCashBox.toString(),
                        selectedItemPortVendingMachine.toString(),
                        selectedItemTypeVendingMachine.toString(),
                        loginRequest,
                        navController,
                    )
                }
            }
        )
    }
}