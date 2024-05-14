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
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndEditTextComposable
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState

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
        viewModel = viewModel,
        state = state,
        navController = navController,
        context = context,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSystemContent(
    viewModel: SettingsViewModel,
    state: SettingsViewState,
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
fun SetupSystemMainContentComposable(
    viewModel: SettingsViewModel,
    navController: NavHostController,
    context: Context,
    state: SettingsViewState,
) {
    var inputVendingMachineCode by remember { mutableStateOf(state.initSetup!!.vendCode) }
    var inputTimeTurnOnLight by remember { mutableStateOf(state.initSetup!!.timeTurnOnLight) }
    var inputTimeTurnOffLight by remember { mutableStateOf(state.initSetup!!.timeTurnOffLight) }
    var inputHighestTempWarning by remember { mutableStateOf(state.initSetup!!.highestTempWarning) }
    var inputLowestTempWarning by remember { mutableStateOf(state.initSetup!!.lowestTempWarning) }
    var inputTemperature by remember { mutableStateOf(state.initSetup!!.temperature) }
    var selectedItemFullScreenAds by remember { mutableStateOf(AnnotatedString(state.initSetup!!.fullScreenAds)) }
    var selectedItemWithdrawalAllowed by remember { mutableStateOf(AnnotatedString(state.initSetup!!.withdrawalAllowed)) }
    var selectedItemAutoStartApplication by remember { mutableStateOf(AnnotatedString(state.initSetup!!.autoStartApplication)) }
    var selectedItemLayoutHomeScreen by remember { mutableStateOf(AnnotatedString(state.initSetup!!.layoutHomeScreen)) }
    var selectedItemDropSensor by remember { mutableStateOf(AnnotatedString(state.initSetup!!.dropSensor)) }
    var selectedItemInchingMode by remember { mutableStateOf(AnnotatedString(state.initSetup!!.inchingMode)) }
    var selectedItemTimeJumpToAdsScreen by remember { mutableStateOf(AnnotatedString(state.initSetup!!.timeToJumpToAdsScreen+"s")) }
    var selectedItemGlassHeatingMode by remember { mutableStateOf(AnnotatedString(state.initSetup!!.glassHeatingMode)) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val appVersionName = packageInfo.versionName
    val keyboardController = LocalSoftwareKeyboardController.current

    val selectedTimeTurnOn = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }
    val selectedTimeTurnOff = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }

    // Function to update the selected time
    val onTimeSelectedTurnOn: (Int, Int) -> Unit = { hour, minute ->
        selectedTimeTurnOn.value = Pair(hour, minute)
    }

    val onTimeSelectedTurnOff: (Int, Int) -> Unit = { hour, minute ->
        selectedTimeTurnOff.value = Pair(hour, minute)
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp - 40.dp

    val partsTurnOnLight = state.initSetup!!.timeTurnOnLight.split(":")
    val hourTurnOnLight = partsTurnOnLight[0].toIntOrNull() ?: 0
    val minuteTurnOnLight = partsTurnOnLight.getOrNull(1)?.toIntOrNull() ?: 0

    val partsTurnOffLight = state.initSetup.timeTurnOffLight.split(":")
    val hourTurnOffLight = partsTurnOffLight[0].toIntOrNull() ?: 0
    val minuteTurnOffLight = partsTurnOffLight.getOrNull(1)?.toIntOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 100.dp)
    ) {
        BodyTextComposable(title = "Application version: $appVersionName", fontWeight = FontWeight.Bold, paddingBottom = 30.dp)

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
        ) {
            viewModel.getInformationOfMachine()
        }

        BodyTextComposable(title = "Serial sim id: ${state.serialSimId}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)

        CustomButtonComposable(
            title = "REFRESH",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) {
            viewModel.getSerialSimId()
        }

        BodyTextComposable(title = "Vending machine code", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        TitleAndEditTextComposable(title = "", paddingBottom = 12.dp, initText = state.initSetup!!.vendCode) {
            inputVendingMachineCode = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
        ) {
            viewModel.updateLayoutHomeInLocal(selectedItemLayoutHomeScreen.toString())
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)) {
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Time to turn on the light", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TimePickerWrapper(
                    defaultHour = hourTurnOnLight,
                    defaultMinute = minuteTurnOnLight,
                    onTimeSelected = onTimeSelectedTurnOn
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Time to turn off the light", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TimePickerWrapper(
                    defaultHour = hourTurnOffLight,
                    defaultMinute = minuteTurnOffLight,
                    onTimeSelected = onTimeSelectedTurnOff
                )
            }
        }

        CustomButtonComposable(
            title = "SAVE",
            titleAlignment = TextAlign.Center,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
        ) {
            Logger.debug("==== ${selectedTimeTurnOn.value.first}")
            Logger.debug("==== ${selectedTimeTurnOn.value.second}")

            Logger.debug("==== ${selectedTimeTurnOff.value.first}")
            Logger.debug("==== ${selectedTimeTurnOff.value.second}")
            viewModel.updateTimeTurnOnTurnOffLightInLocal(
                timeTurnOnLight = selectedTimeTurnOn.value.first.toString() + ":" + selectedTimeTurnOn.value.second.toString(),
                timeTurnOffLight = selectedTimeTurnOff.value.first.toString() + ":" + selectedTimeTurnOff.value.second.toString(),
            )
        }

        CustomButtonComposable(
            title = "CHECK THE DROP SENSOR",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
        ) {
            viewModel.updateInchingModeInLocal(selectedItemInchingMode.toString())
        }

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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
        ) {
            viewModel.updateGlassHeatingModeInLocal(selectedItemGlassHeatingMode.toString())
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)) {

            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Lowest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                EditTextComposable(initText = state.initSetup.lowestTempWarning, keyboardTypeNumber = true) {
                    inputLowestTempWarning = it
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.width(screenWidthDp/2)) {
                BodyTextComposable(title = "Highest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                EditTextComposable(initText = state.initSetup.highestTempWarning, keyboardTypeNumber = true) {
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
            paddingBottom = 30.dp,
        ) {
//            viewModel.updateGlassHeatingModeInLocal(selectedItemGlassHeatingMode.toString())
        }

        BodyTextComposable(title = "Temperature", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        TitleAndEditTextComposable(title = "", paddingBottom = 12.dp, initText = state.initSetup.temperature) {
            inputTemperature = it
        }
        CustomButtonComposable(
            title = "SAVE",
            wrap = true,
            cornerRadius = 4.dp,
            height = 60.dp,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
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
            paddingBottom = 30.dp,
        ) {
            viewModel.showDialogConfirm("Are you sure to reset factory?", null, "resetFactory")
        }
    }
}

@Composable
fun TimePickerWrapper(
    defaultHour: Int,
    defaultMinute: Int,
    onTimeSelected: (hourOfDay: Int, minute: Int) -> Unit
) {
    AndroidView(factory = { context ->
        TimePicker(context).apply {
            setIs24HourView(true)
            hour = defaultHour
            minute = defaultMinute
            setOnTimeChangedListener { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            }
        }
    })
}