package com.leduytuanvu.vendingmachine.features.settings.presentation.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
internal fun SetupSystemScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SetupSystemContent(
        state = state,
        navController = navController,
        context = context,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSystemContent(
    state: SettingsViewState,
    navController: NavHostController,
    context: Context,
) {
//    var selectedItemTypeVendingMachine by remember { mutableStateOf(AnnotatedString("TCN")) }
//    var selectedItemPortCashBox by remember { mutableStateOf(AnnotatedString("TTYS2")) }
//    var selectedItemPortVendingMachine by remember { mutableStateOf(AnnotatedString("TTYS1")) }
//
//    val itemsPort = listOf(
//        AnnotatedString("TTYS1"),
//        AnnotatedString("TTYS2"),
//        AnnotatedString("TTYS3"),
//        AnnotatedString("TTYS4")
//    )
//    val itemsTypeVendingMachine = listOf(
//        AnnotatedString("XY"),
//        AnnotatedString("TCN"),
//        AnnotatedString("TCN INTEGRATED CIRCUITS"),
//    )

    LoadingDialogComposable(isLoading = state.isLoading)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            content = {
                SetupSystemMainContentComposable(navController = navController, context = context, state = state)
                SetupSystemBackContentComposable(navController = navController)
            }
        )
    }
}

@Composable
fun SetupSystemBackContentComposable(navController: NavHostController) {
    Box (modifier = Modifier.background(Color.White).fillMaxWidth()) {
        CustomButtonComposable(
            title = "BACK",
            wrap = true,
            height = 65.dp,
            fontSize = 20.sp,
            cornerRadius = 4.dp,
            paddingBottom = 20.dp,
            fontWeight = FontWeight.Bold,
        ) {
            navController.popBackStack()
        }
    }
}

@Composable
fun SetupSystemMainContentComposable(navController: NavHostController, context: Context, state: SettingsViewState) {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val appVersionName = packageInfo.versionName
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 100.dp)
    ) {
        BodyTextComposable(title = "Application version: $appVersionName", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        BodyTextComposable(title = "Information of vending machine", fontWeight = FontWeight.Bold)
        BodyTextComposable(title = "Id: ")
        BodyTextComposable(title = "Code: ")
        BodyTextComposable(title = "Company name: ")
        BodyTextComposable(title = "Hotline: ")
        BodyTextComposable(title = "Description: ")

        Spacer(modifier = Modifier.height(16.dp))

        BodyTextComposable(title = "Serial sim id: ${state.androidId}", fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        BodyTextComposable(title = "Vending machine code")
        BodyTextComposable(title = "Id: ")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Vending machine code")
        BodyTextComposable(title = "Turn on")
        BodyTextComposable(title = "Turn off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Withdrawal allowed")
        BodyTextComposable(title = "Turn on")
        BodyTextComposable(title = "Turn off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Automatically start the application")
        BodyTextComposable(title = "Turn on")
        BodyTextComposable(title = "Turn off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Layout screen")
        BodyTextComposable(title = "3")
        BodyTextComposable(title = "4")
        BodyTextComposable(title = "5")
        BodyTextComposable(title = "6")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Time to turn on the light")
        BodyTextComposable(title = "Time to turn off the light")
        CustomButtonComposable(title = "SAVE") {

        }

        CustomButtonComposable(title = "Check the drop sensor") {

        }

        BodyTextComposable(title = "Drop sensor")
        BodyTextComposable(title = "Time to turn on the light")
        BodyTextComposable(title = "Time to turn off the light")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Inching mode")
        BodyTextComposable(title = "0,1,2,3,4,5")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Time to jump to the advertising screen")
        BodyTextComposable(title = "30-120s")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Glass heating mode")
        BodyTextComposable(title = "on,off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Glass heating mode")
        BodyTextComposable(title = "on,off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Temperature warning")
        BodyTextComposable(title = "thấp nhất, cao nhất,off")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "Temperature")
        BodyTextComposable(title = "edit text")
        CustomButtonComposable(title = "SAVE") {

        }

        BodyTextComposable(title = "đọc nhiệt độ")
        BodyTextComposable(title = "temp1,temp2")
        CustomButtonComposable(title = "SAVE") {

        }

        CustomButtonComposable(title = "Reset initial configuration") {

        }
    }
}