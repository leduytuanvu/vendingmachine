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
import androidx.compose.runtime.LaunchedEffect
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
import com.leduytuanvu.vendingmachine.common.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleAndEditTextComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
internal fun SetupSystemScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(key1 = viewModel) {
        viewModel.getInformationOfMachine()
    }
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
    Box (modifier = Modifier
        .background(Color.White)
        .fillMaxWidth()) {
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
    val itemsPort = listOf(
        AnnotatedString("TTYS1"),
        AnnotatedString("TTYS2"),
        AnnotatedString("TTYS3"),
        AnnotatedString("TTYS4")
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 100.dp)
    ) {
        BodyTextComposable(title = "Application version: $appVersionName", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Information of vending machine", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)

        BodyTextComposable(title = "Id: ${state.informationOfMachine?.id ?: ""}", paddingBottom = 8.dp)
        BodyTextComposable(title = "Code: ${state.informationOfMachine?.code ?: ""}", paddingBottom = 8.dp)
        BodyTextComposable(title = "Company name: ${state.informationOfMachine?.companyName ?: ""}", paddingBottom = 8.dp)
        BodyTextComposable(title = "Hotline: ${state.informationOfMachine?.hotline ?: ""}", paddingBottom = 8.dp)
        BodyTextComposable(title = "Description: ${state.informationOfMachine?.description ?: ""}", paddingBottom = 10.dp)

        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Serial sim id: ${state.serialSimId}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)

        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Vending machine code", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        TitleAndEditTextComposable(title = "", paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Full screen ads", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = AnnotatedString("ON"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Withdrawal allowed", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = AnnotatedString("ON"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Automatically start the application", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = AnnotatedString("ON"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Layout screen", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("3"),
            AnnotatedString("4"),
            AnnotatedString("5"),
            AnnotatedString("6"),
        ), selectedItem = AnnotatedString("3"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }


        BodyTextComposable(title = "Time to turn on the light")
        BodyTextComposable(title = "Time to turn off the light")
        CustomButtonComposable(title = "SAVE") {

        }

        CustomButtonComposable(
            title = "CHECK THE DROP SENSOR",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Drop sensor", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = AnnotatedString("ON"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Inching mode", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("0"),
            AnnotatedString("1"),
            AnnotatedString("2"),
            AnnotatedString("3"),
            AnnotatedString("4"),
            AnnotatedString("5"),
        ), selectedItem = AnnotatedString("0"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Time to jump to the advertising screen", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("30s"),
            AnnotatedString("60s"),
            AnnotatedString("90s"),
            AnnotatedString("120s"),
            AnnotatedString("150s"),
            AnnotatedString("180s"),
            AnnotatedString("210s"),
            AnnotatedString("240s"),
            AnnotatedString("270s"),
            AnnotatedString("300s"),
        ), selectedItem = AnnotatedString("30s"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

        BodyTextComposable(title = "Glass heating mode", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = AnnotatedString("ON"), paddingTop = 2.dp, paddingBottom = 12.dp) {

        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) { }

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