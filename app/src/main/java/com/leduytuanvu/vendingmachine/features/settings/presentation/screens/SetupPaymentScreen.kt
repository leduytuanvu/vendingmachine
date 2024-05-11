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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.composables.BodyTextComposable
import com.leduytuanvu.vendingmachine.common.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
internal fun SetupPaymentScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SetupPaymentContent(
        state = state,
        navController = navController
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupPaymentContent(
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

                BodyTextComposable(title = "Current cash")
                BodyTextComposable(title = "100000: ")
                CustomButtonComposable(title = "Reset current cash") {

                }

                BodyTextComposable(title = "Rotten box balance")
                BodyTextComposable(title = "Version")
                CustomButtonComposable(title = "Refresh") {

                }

                BodyTextComposable(title = "Time out payment online")
                BodyTextComposable(title = "30,60,90,120,180,300")
                CustomButtonComposable(title = "Save") {

                }

                BodyTextComposable(title = "Set time reset on everyday")
                BodyTextComposable(title = "Id: ")
                CustomButtonComposable(title = "SAVE") {

                }


            }
        )
    }
}