package com.leduytuanvu.vendingmachine.features.settings.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.leduytuanvu.vendingmachine.common.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
internal fun SetupPortScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SetupPortContent(
        state = state,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupPortContent(
    state: SettingsViewState,
    navController: NavHostController,
) {
    var selectedItemTypeVendingMachine by remember { mutableStateOf(AnnotatedString("TCN")) }
    var selectedItemPortCashBox by remember { mutableStateOf(AnnotatedString("TTYS2")) }
    var selectedItemPortVendingMachine by remember { mutableStateOf(AnnotatedString("TTYS1")) }

    val itemsPort = listOf(
        AnnotatedString("TTYS1"),
        AnnotatedString("TTYS2"),
        AnnotatedString("TTYS3"),
        AnnotatedString("TTYS4")
    )
    val itemsTypeVendingMachine = listOf(
        AnnotatedString("XY"),
        AnnotatedString("TCN"),
        AnnotatedString("TCN INTEGRATED CIRCUITS"),
    )

    LoadingDialogComposable(isLoading = state.isLoading)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) {
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

                }
            }
        )
    }
}