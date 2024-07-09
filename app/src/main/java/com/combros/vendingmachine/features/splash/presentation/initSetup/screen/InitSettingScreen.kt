package com.combros.vendingmachine.features.splash.presentation.initSetup.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
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
import com.combros.vendingmachine.common.base.presentation.composables.*
import com.combros.vendingmachine.features.auth.data.model.request.LoginRequest
import com.combros.vendingmachine.core.util.itemsPort
import com.combros.vendingmachine.core.util.itemsTypeVendingMachine
import com.combros.vendingmachine.features.splash.presentation.initSetup.viewModel.InitSetupViewModel
import com.combros.vendingmachine.features.splash.presentation.initSetup.viewState.InitSetupViewState

@Composable
internal fun InitSetupScreen(
    navController: NavHostController,
    viewModel: InitSetupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    InitSetupContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InitSetupContent(
    state: InitSetupViewState,
    viewModel: InitSetupViewModel,
    navController: NavHostController,
) {
    var inputVendingMachineCode by remember { mutableStateOf("") }
    var inputUsername by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var selectedItemTypeVendingMachine by remember { mutableStateOf(AnnotatedString("TCN")) }
    var selectedItemPortCashBox by remember { mutableStateOf(AnnotatedString("ttyS2")) }
    var selectedItemPortVendingMachine by remember { mutableStateOf(AnnotatedString("ttyS1")) }
    viewModel.getAndroidId()
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = { viewModel.hideDialogWarning() },
    )
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            content = {
                TitleTextComposable(title = "INIT SETUP FOR VENDING MACHINE")
                Spacer(modifier = Modifier.height(10.dp))
                BodyTextComposable(title = "Android id: ${state.androidId}")
                Spacer(modifier = Modifier.height(14.dp))
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
                    height = 70.dp,
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