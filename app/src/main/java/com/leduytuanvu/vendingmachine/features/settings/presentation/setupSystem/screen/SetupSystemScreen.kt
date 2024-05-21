package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.screen

import android.annotation.SuppressLint
import android.widget.TimePicker
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.EditTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TimePickerWrapperComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndEditTextComposable
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewModel.SetupSystemViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState

@Composable
internal fun SetupSystemScreen(
    navController: NavHostController,
    viewModel: SetupSystemViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.loadInitData()
    }
    SetupSystemContent(
        viewModel = viewModel,
        state = state,
        navController = navController,
        context = context,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSystemContent(
    viewModel: SetupSystemViewModel,
    state: SetupSystemViewState,
    navController: NavHostController,
    context: Context,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
//    ConfirmDialogComposable(isConfirm = state.isConfirm, state, viewModel)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            content = {
                SetupSystemMainContentComposable(
                    navController = navController,
                    context = context,
                    state = state,
                    viewModel = viewModel
                )
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
            height = 70.dp,
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
fun SetupSystemMainContentComposable(
    viewModel: SetupSystemViewModel,
    navController: NavHostController,
    context: Context,
    state: SetupSystemViewState,
) {
    var inputVendingMachineCode by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.vendCode else "") }
    var inputTimeTurnOnLight by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.timeTurnOnLight else "0:0") }
    var inputTimeTurnOffLight by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.vendCode else "0:0") }
    var inputHighestTempWarning by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.highestTempWarning else "") }
    var inputLowestTempWarning by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.lowestTempWarning else "") }
    var inputTemperature by remember { mutableStateOf(if(state.initSetup!=null) state.initSetup.temperature else "") }
    var selectedItemFullScreenAds by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.fullScreenAds else "ON")) }
    var selectedItemWithdrawalAllowed by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.withdrawalAllowed else "ON")) }
    var selectedItemAutoStartApplication by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.autoStartApplication else "ON")) }
    var selectedItemLayoutHomeScreen by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.layoutHomeScreen else "3")) }
    var selectedItemDropSensor by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.dropSensor else "ON")) }
    var selectedItemInchingMode by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.inchingMode else "0")) }
    var selectedItemTimeJumpToAdsScreen by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.timeoutJumpToBigAdsScreen else "60s")) }
    var selectedItemGlassHeatingMode by remember { mutableStateOf(AnnotatedString(if(state.initSetup!=null) state.initSetup.glassHeatingMode else "ON")) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val appVersionName = packageInfo.versionName
    val keyboardController = LocalSoftwareKeyboardController.current
//    val selectedTimeTurnOn = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }
//    val selectedTimeTurnOff = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }
//    // Function to update the selected time
//    val onTimeSelectedTurnOn: (Int, Int) -> Unit = { hour, minute ->
//        selectedTimeTurnOn.value = Pair(hour, minute)
//    }
//    val onTimeSelectedTurnOff: (Int, Int) -> Unit = { hour, minute ->
//        selectedTimeTurnOff.value = Pair(hour, minute)
//    }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp - 40.dp
//    val partsTurnOnLight = state.initSetup!!.timeTurnOnLight.split(":")
//    val hourTurnOnLight = partsTurnOnLight[0].toIntOrNull() ?: 0
//    val minuteTurnOnLight = partsTurnOnLight.getOrNull(1)?.toIntOrNull() ?: 0
//    val partsTurnOffLight = state.initSetup.timeTurnOffLight.split(":")
//    val hourTurnOffLight = partsTurnOffLight[0].toIntOrNull() ?: 0
//    val minuteTurnOffLight = partsTurnOffLight.getOrNull(1)?.toIntOrNull() ?: 0
    val seralSimId = state.serialSimId.ifEmpty { "No sim found" }

    LaunchedEffect(state.initSetup) {
        inputVendingMachineCode = state.initSetup?.vendCode ?: ""
        inputTimeTurnOnLight = state.initSetup?.timeTurnOnLight ?: "0:0"
        inputTimeTurnOffLight = state.initSetup?.timeTurnOffLight ?: "0:0"
        inputHighestTempWarning = state.initSetup?.highestTempWarning ?: ""
        inputLowestTempWarning = state.initSetup?.lowestTempWarning ?: ""
        inputTemperature = state.initSetup?.temperature ?: ""
        selectedItemFullScreenAds = AnnotatedString(state.initSetup?.fullScreenAds ?: "ON")
        selectedItemWithdrawalAllowed = AnnotatedString(state.initSetup?.withdrawalAllowed ?: "ON")
        selectedItemAutoStartApplication = AnnotatedString(state.initSetup?.autoStartApplication ?: "ON")
        selectedItemLayoutHomeScreen = AnnotatedString(state.initSetup?.layoutHomeScreen ?: "3")
        selectedItemDropSensor = AnnotatedString(state.initSetup?.dropSensor ?: "ON")
        selectedItemInchingMode = AnnotatedString(state.initSetup?.inchingMode ?: "0")
        selectedItemTimeJumpToAdsScreen = AnnotatedString(state.initSetup?.timeoutJumpToBigAdsScreen ?: "60s")
        selectedItemGlassHeatingMode = AnnotatedString(state.initSetup?.glassHeatingMode ?: "ON")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 100.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        BodyTextComposable(
            title = "Application version: $appVersionName", 
            fontWeight = FontWeight.Bold, 
            paddingBottom = 50.dp,
        )

        BodyTextComposable(title = "Serial sim id: ${seralSimId}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.getSerialSimId()
        }

        BodyTextComposable(title = "Information of vending machine", fontWeight = FontWeight.Bold, paddingBottom = 12.dp)
        BodyTextComposable(title = "Id: ${state.informationOfMachine?.id ?: ""}", paddingBottom = 10.dp)
        BodyTextComposable(title = "Code: ${state.informationOfMachine?.code ?: ""}", paddingBottom = 10.dp)
        BodyTextComposable(title = "Company name: ${state.informationOfMachine?.companyName ?: ""}", paddingBottom = 10.dp)
        BodyTextComposable(title = "Hotline: ${state.informationOfMachine?.hotline ?: ""}", paddingBottom = 10.dp)
        BodyTextComposable(title = "Description: ${state.informationOfMachine?.description ?: ""}", paddingBottom = 12.dp)

        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.getInformationOfMachine()
        }



        BodyTextComposable(title = "Vending machine code", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if(state.initSetup!=null) {
            TitleAndEditTextComposable(
                title = "",
                paddingBottom = 12.dp,
                initText = state.initSetup.vendCode
            ) {
                inputVendingMachineCode = it
            }
        } else {
            TitleAndEditTextComposable(
                title = "",
                paddingBottom = 12.dp,
                initText = ""
            ) {
                inputVendingMachineCode = it
            }
        }

        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            keyboardController?.hide()
            viewModel.updateVendCodeInLocal(inputVendingMachineCode)
        }

        BodyTextComposable(title = "Full screen ads", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = selectedItemFullScreenAds, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemFullScreenAds = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateFullScreenAdsInLocal(selectedItemFullScreenAds.toString())
        }

        BodyTextComposable(title = "Withdrawal allowed", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = selectedItemWithdrawalAllowed, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemWithdrawalAllowed = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateWithdrawalAllowedInLocal(selectedItemWithdrawalAllowed.toString())
        }

        BodyTextComposable(title = "Automatically start the application", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = selectedItemAutoStartApplication, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemAutoStartApplication = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateAutoStartApplicationInLocal(selectedItemAutoStartApplication.toString())
        }

        BodyTextComposable(title = "Layout home screen", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("3"),
            AnnotatedString("4"),
            AnnotatedString("5"),
            AnnotatedString("6"),
        ), selectedItem = selectedItemLayoutHomeScreen, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemLayoutHomeScreen = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateLayoutHomeInLocal(selectedItemLayoutHomeScreen.toString())
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)) {
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Time to turn on the light", fontWeight = FontWeight.Bold, paddingBottom = 10.dp)
//                TimePickerWrapperComposable(
//                    defaultHour = hourTurnOnLight,
//                    defaultMinute = minuteTurnOnLight,
//                    onTimeSelected = onTimeSelectedTurnOn
//                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Time to turn off the light", fontWeight = FontWeight.Bold, paddingBottom = 10.dp)
//                TimePickerWrapperComposable(
//                    defaultHour = hourTurnOffLight,
//                    defaultMinute = minuteTurnOffLight,
//                    onTimeSelected = onTimeSelectedTurnOff
//                )
            }
        }

        CustomButtonComposable(
            title = "SAVE",
            titleAlignment = TextAlign.Center,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
//            viewModel.updateTimeTurnOnTurnOffLightInLocal(
//                timeTurnOnLight = selectedTimeTurnOn.value.first.toString() + ":" + selectedTimeTurnOn.value.second.toString(),
//                timeTurnOffLight = selectedTimeTurnOff.value.first.toString() + ":" + selectedTimeTurnOff.value.second.toString(),
//            )
        }

        CustomButtonComposable(
            title = "CHECK THE DROP SENSOR",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.check()
        }

        BodyTextComposable(title = "Drop sensor", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = selectedItemDropSensor, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemDropSensor = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateDropSensorInLocal(selectedItemDropSensor.toString())
        }

        BodyTextComposable(title = "Inching mode", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("0"),
            AnnotatedString("1"),
            AnnotatedString("2"),
            AnnotatedString("3"),
            AnnotatedString("4"),
            AnnotatedString("5"),
        ), selectedItem = selectedItemInchingMode, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemInchingMode = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateInchingModeInLocal(selectedItemInchingMode.toString())
        }

        BodyTextComposable(title = "Time to jump to the advertising screen", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("10s"),
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
        ), selectedItem = selectedItemTimeJumpToAdsScreen, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemTimeJumpToAdsScreen = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateTimeJumpToAdsScreenInLocal(selectedItemTimeJumpToAdsScreen.toString().substringBefore("s"))
        }

        BodyTextComposable(title = "Glass heating mode", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        TitleAndDropdownComposable(title = "", items = listOf(
            AnnotatedString("ON"),
            AnnotatedString("OFF"),
        ), selectedItem = selectedItemGlassHeatingMode, paddingTop = 2.dp, paddingBottom = 12.dp) {
            selectedItemGlassHeatingMode = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateGlassHeatingModeInLocal(selectedItemGlassHeatingMode.toString())
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)) {

            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Lowest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                EditTextComposable(initText = if(state.initSetup!=null) state.initSetup.lowestTempWarning else "", keyboardTypeNumber = true) {
                    inputLowestTempWarning = it
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Highest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                EditTextComposable(initText =if(state.initSetup!=null) state.initSetup.highestTempWarning else "", keyboardTypeNumber = true) {
                    inputHighestTempWarning = it
                }
            }
        }
        CustomButtonComposable(
            title = "SAVE",
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            viewModel.updateHighestAndLowestTempWarningInLocal(inputHighestTempWarning, inputLowestTempWarning)
        }

        BodyTextComposable(title = "Temperature", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        TitleAndEditTextComposable(title = "", paddingBottom = 12.dp, initText = if(state.initSetup!=null) state.initSetup.temperature else "", keyboardTypeNumber = true) {
            inputTemperature = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            keyboardController?.hide()
            viewModel.updateTemperatureInLocal(inputTemperature)
        }


        BodyTextComposable(title = "Temperature 1: ", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        BodyTextComposable(title = "Temperature 2: ", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
        CustomButtonComposable(
            title = "READ TEMPERATURE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 50.dp,
        ) {
            keyboardController?.hide()
            viewModel.updateTemperatureInLocal(inputTemperature)
        }

        CustomButtonComposable(
            title = "RESET FACTORY",
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            titleAlignment = TextAlign.Center,
            fontSize = 20.sp,
            paddingBottom = 20.dp,
        ) {
            viewModel.showDialogConfirm("Are you sure to reset factory?", null, "resetFactory")
        }
    }
}