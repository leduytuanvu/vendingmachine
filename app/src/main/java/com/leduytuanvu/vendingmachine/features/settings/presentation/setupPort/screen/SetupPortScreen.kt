package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.itemsPort
import com.leduytuanvu.vendingmachine.core.util.itemsTypeVendingMachine
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewModel.SetupPortViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.viewState.SetupPortViewState

@Composable
internal fun SetupPortScreen(
    navController: NavHostController,
    viewModel: SetupPortViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Logger.info("SetupPortScreen")
    SetupPortContent(
        state = state,
        viewModel = viewModel,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupPortContent(
    state: SetupPortViewState,
    viewModel: SetupPortViewModel,
    navController: NavHostController,
) {
    var selectedItemTypeVendingMachine by remember {
        mutableStateOf(
            AnnotatedString(state.initSetup?.typeVendingMachine!!)
        )
    }
    var selectedItemPortCashBox by remember {
        mutableStateOf(
            AnnotatedString(state.initSetup?.portCashBox!!)
        )
    }
    var selectedItemPortVendingMachine by remember {
        mutableStateOf(
            AnnotatedString(state.initSetup?.portVendingMachine!!)
        )
    }

    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            content = {
                CustomButtonComposable(
                    title = "BACK",
                    wrap = true,
                    height = 65.dp,
                    fontSize = 20.sp,
                    cornerRadius = 4.dp,
                    fontWeight = FontWeight.Bold,
                ) {
                    navController.popBackStack()
                }
                Spacer(modifier = Modifier.height(30.dp))
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
                    title = "SAVE SETUP PORT",
                    titleAlignment = TextAlign.Center,
                    paddingBottom = 20.dp,
                    cornerRadius = 4.dp,
                    height = 65.dp,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                ) {
                    viewModel.saveSetupPort(
                        typeVendingMachine = selectedItemTypeVendingMachine.toString(),
                        portCashBox = selectedItemPortCashBox.toString(),
                        portVendingMachine = selectedItemPortVendingMachine.toString(),
                    )
                }
            }
        )
    }
}