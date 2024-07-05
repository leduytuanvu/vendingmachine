package com.combros.vendingmachine.features.settings.presentation.setupSystem.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.EditTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.TimePickerWrapperComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleAndEditTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.features.settings.presentation.setupSystem.viewModel.SetupSystemViewModel
import com.combros.vendingmachine.features.settings.presentation.setupSystem.viewState.SetupSystemViewState
import kotlinx.coroutines.delay

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
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.closePort()
        }
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.SetupSystemScreenRoute.route) {
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
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                lastInteractionTime = System.currentTimeMillis()
                return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                lastInteractionTime = System.currentTimeMillis()
                return super.onPostScroll(consumed, available, source)
            }
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
        SetupSystemContent(
            viewModel = viewModel,
            state = state,
            navController = navController,
            context = context,
            onClick = { lastInteractionTime = System.currentTimeMillis() },
            nestedScrollConnection = nestedScrollConnection,
        )
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSystemContent(
    viewModel: SetupSystemViewModel,
    state: SetupSystemViewState,
    navController: NavHostController,
    context: Context,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = {
            onClick()
            viewModel.hideDialogWarning()
        },
    )
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = {
            onClick()
            viewModel.hideDialogConfirm()
        },
        onClickConfirm = {
            onClick()
            viewModel.resetFactory(navController)
        },
    )
    Scaffold(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                }
            )
        }.nestedScroll(nestedScrollConnection),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp).pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )
                }.nestedScroll(nestedScrollConnection),
            content = {
                SetupSystemMainContentComposable(
                    context = context,
                    state = state,
                    viewModel = viewModel,
                    onClick = { onClick() },
                    nestedScrollConnection = nestedScrollConnection,
                )
                SetupSystemBackContentComposable(
                    navController = navController,
                    onClick = { onClick() },
                    nestedScrollConnection = nestedScrollConnection,
                )
            }
        )
    }
}

@Composable
fun SetupSystemBackContentComposable(
    navController: NavHostController,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    Box (modifier = Modifier
        .background(Color.White)
        .fillMaxWidth().pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                }
            )
        }) {
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
    context: Context,
    state: SetupSystemViewState,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    var inputVendingMachineCode by remember { mutableStateOf("") }
    var inputTimeTurnOnLight by remember { mutableStateOf("00:00") }
    var inputTimeTurnOffLight by remember { mutableStateOf("00:00") }
    var inputHighestTempWarning by remember { mutableStateOf("") }
    var inputLowestTempWarning by remember { mutableStateOf("") }
    var inputTemperature by remember { mutableStateOf("") }
    var selectedItemFullScreenAds by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemAutoTurnOnTurnOffLight by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemAutoResetAppEveryday by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemWithdrawalAllowed by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemAutoStartApplication by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemLayoutHomeScreen by remember { mutableStateOf(AnnotatedString("3")) }
    var selectedItemDropSensor by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemInchingMode by remember { mutableStateOf(AnnotatedString("0")) }
    var selectedItemTimeJumpToAdsScreen by remember { mutableStateOf(AnnotatedString("60s")) }
    var selectedItemGlassHeatingMode by remember { mutableStateOf(AnnotatedString("ON")) }
    var selectedItemBigAds by remember { mutableStateOf(AnnotatedString("ON")) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val appVersionName = packageInfo.versionName
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedTimeTurnOn = remember { mutableStateOf(Pair(0, 0)) }
    val selectedTimeTurnOff = remember { mutableStateOf(Pair(0, 0)) }
    val onTimeSelectedTurnOn: (Int, Int) -> Unit = { hour, minute -> selectedTimeTurnOn.value = Pair(hour, minute) }
    val onTimeSelectedTurnOff: (Int, Int) -> Unit = { hour, minute -> selectedTimeTurnOff.value = Pair(hour, minute) }
    var partsTurnOnLight by remember { mutableStateOf(listOf("0", "0")) }
    var partsTurnOffLight by remember { mutableStateOf(listOf("0", "0")) }
    var hourTurnOnLight by remember { mutableIntStateOf(0) }
    var minuteTurnOnLight by remember { mutableIntStateOf(0) }
    var hourTurnOffLight by remember { mutableIntStateOf(0) }
    var minuteTurnOffLight by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp - 40.dp
    val seralSimId = state.serialSimId.ifEmpty { "No sim found" }
    val selectedTimeReset = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }
    val onTimeSelectedReset: (Int, Int) -> Unit = { hour, minute ->
        selectedTimeReset.value = Pair(hour, minute)
    }
    var partsReset by remember {
        mutableStateOf(if (state.initSetup != null) state.initSetup.timeResetOnEveryDay.split(":") else listOf("0", "0"))
    }
    var hourReset by remember { mutableIntStateOf(partsReset[0].toIntOrNull() ?: 0) }
    var minuteReset by remember { mutableIntStateOf(partsReset.getOrNull(1)?.toIntOrNull() ?: 0) }

    LaunchedEffect(state.initSetup) {
        inputVendingMachineCode = state.initSetup?.vendCode ?: ""
        inputTimeTurnOnLight = state.initSetup?.timeTurnOnLight ?: "0:0"
        inputTimeTurnOffLight = state.initSetup?.timeTurnOffLight ?: "0:0"
        inputHighestTempWarning = state.initSetup?.highestTempWarning ?: ""
        inputLowestTempWarning = state.initSetup?.lowestTempWarning ?: ""
        inputTemperature = state.initSetup?.temperature ?: ""
        selectedItemFullScreenAds = AnnotatedString(state.initSetup?.fullScreenAds ?: "ON")
        selectedItemAutoResetAppEveryday = AnnotatedString(state.initSetup?.autoResetAppEveryday ?: "ON")
        selectedItemAutoTurnOnTurnOffLight = AnnotatedString(state.initSetup?.autoTurnOnTurnOffLight ?: "ON")
        selectedItemWithdrawalAllowed = AnnotatedString(state.initSetup?.withdrawalAllowed ?: "ON")
        selectedItemAutoStartApplication = AnnotatedString(state.initSetup?.autoStartApplication ?: "ON")
        selectedItemLayoutHomeScreen = AnnotatedString(state.initSetup?.layoutHomeScreen ?: "3")
        selectedItemDropSensor = AnnotatedString(state.initSetup?.dropSensor ?: "ON")
        selectedItemInchingMode = AnnotatedString(state.initSetup?.inchingMode ?: "0")
        selectedItemTimeJumpToAdsScreen = AnnotatedString(if(state.initSetup?.timeoutJumpToBigAdsScreen!=null) "${state.initSetup.timeoutJumpToBigAdsScreen}s" else "60s")
        selectedItemGlassHeatingMode = AnnotatedString(state.initSetup?.glassHeatingMode ?: "ON")
        selectedItemBigAds = AnnotatedString(state.initSetup?.fullScreenAds ?: "ON")
        partsTurnOnLight = if (state.initSetup?.timeTurnOnLight != null) state.initSetup.timeTurnOnLight.split(":") else listOf("0", "0")
        hourTurnOnLight = partsTurnOnLight[0].toIntOrNull() ?: 0
        minuteTurnOnLight = partsTurnOnLight.getOrNull(1)?.toIntOrNull() ?: 0
        partsTurnOffLight = if (state.initSetup?.timeTurnOffLight != null) state.initSetup.timeTurnOffLight.split(":") else listOf("0", "0")
        hourTurnOffLight = partsTurnOffLight[0].toIntOrNull() ?: 0
        minuteTurnOffLight = partsTurnOffLight.getOrNull(1)?.toIntOrNull() ?: 0
        partsReset = if (state.initSetup?.timeResetOnEveryDay != null) state.initSetup.timeResetOnEveryDay.split(":") else listOf("0", "0")
        hourReset = partsReset[0].toIntOrNull() ?: 0
        minuteReset = partsReset.getOrNull(1)?.toIntOrNull() ?: 0
//        Logger.info("hour reset: $hourReset, minute reset: $minuteReset")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 100.dp).pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }.nestedScroll(nestedScrollConnection),
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        if(state.initSetup!=null) {
            if(state.initSetup.role == "admin") {
                BodyTextComposable(
                    title = "Application version: $appVersionName",
                    fontWeight = FontWeight.Bold,
                    paddingBottom = 50.dp,
                )

                BodyTextComposable(
                    title = "Android id: ${if(state.initSetup!=null) state.initSetup.androidId ?: "" else ""}",
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
                    onClick()
                    viewModel.getSerialSimId()
                }

                BodyTextComposable(title = "Information of vending machine", fontWeight = FontWeight.Bold, paddingBottom = 12.dp)
                BodyTextComposable(title = "Id: ${state.informationOfMachine?.id ?: ""}", paddingBottom = 10.dp)
                BodyTextComposable(title = "Android id: ${state.informationOfMachine?.androidId ?: ""}", paddingBottom = 10.dp)
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
                    onClick()
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
                        onClick()
                        inputVendingMachineCode = it
                    }
                } else {
                    TitleAndEditTextComposable(
                        title = "",
                        paddingBottom = 12.dp,
                        initText = ""
                    ) {
                        onClick()
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
                    onClick()
                    keyboardController?.hide()
                    viewModel.updateVendCodeInLocal(inputVendingMachineCode)
                }

//        BodyTextComposable(title = "Full screen ads", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
//        TitleAndDropdownComposable(title = "", items = listOf(
//            AnnotatedString("ON"),
//            AnnotatedString("OFF"),
//        ), selectedItem = selectedItemFullScreenAds, paddingTop = 2.dp, paddingBottom = 12.dp) {
//            selectedItemFullScreenAds = it
//        }
//        CustomButtonComposable(
//            title = "SAVE",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            viewModel.updateFullScreenAdsInLocal(selectedItemFullScreenAds.toString())
//        }

                BodyTextComposable(title = "Withdrawal allowed", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemWithdrawalAllowed, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
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
                    onClick()
                    viewModel.updateWithdrawalAllowedInLocal(selectedItemWithdrawalAllowed.toString())
                }

                BodyTextComposable(title = "Automatically start the application", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemAutoStartApplication, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
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
                    onClick()
                    viewModel.updateAutoStartApplicationInLocal(selectedItemAutoStartApplication.toString())
                }

                BodyTextComposable(title = "Layout home screen", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("3"),
                    AnnotatedString("4"),
                    AnnotatedString("5"),
                    AnnotatedString("6"),
                ), selectedItem = selectedItemLayoutHomeScreen, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
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
                    onClick()
                    viewModel.updateLayoutHomeInLocal(selectedItemLayoutHomeScreen.toString())
                }

                BodyTextComposable(title = "Auto turn on light and turn off light", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemAutoTurnOnTurnOffLight, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
                    selectedItemAutoTurnOnTurnOffLight = it
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
                    onClick()
                    viewModel.updateAutoTurnOnTurnOffLightInLocal(selectedItemAutoTurnOnTurnOffLight.toString())
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {
                    Column(modifier = Modifier.width(screenWidthDp/2).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {
                        BodyTextComposable(title = "Time to turn on the light", fontWeight = FontWeight.Bold, paddingBottom = 10.dp)
//                Logger.debug("hour turn on: $hourTurnOnLight, minute: $minuteTurnOnLight")
                        if(state.initSetup!=null) {
                            TimePickerWrapperComposable(
                                defaultHour = state.initSetup.timeTurnOnLight.split(":")[0].toInt(),
                                defaultMinute = state.initSetup.timeTurnOnLight.split(":")[1].toInt(),
                                onTimeSelected = onTimeSelectedTurnOn
                            )
                        } else {
                            TimePickerWrapperComposable(
                                defaultHour = 0,
                                defaultMinute = 0,
                                onTimeSelected = onTimeSelectedTurnOn
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.width(screenWidthDp/2).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {
                        BodyTextComposable(title = "Time to turn off the light", fontWeight = FontWeight.Bold, paddingBottom = 10.dp)
//                Logger.debug("hour turn off: $hourTurnOffLight, minute: $minuteTurnOffLight")
                        if(state.initSetup!=null) {
                            TimePickerWrapperComposable(
                                defaultHour = state.initSetup.timeTurnOffLight.split(":")[0].toInt(),
                                defaultMinute = state.initSetup.timeTurnOffLight.split(":")[1].toInt(),
                                onTimeSelected = onTimeSelectedTurnOff
                            )
                        } else {
                            TimePickerWrapperComposable(
                                defaultHour = 0,
                                defaultMinute = 0,
                                onTimeSelected = onTimeSelectedTurnOff
                            )
                        }
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
                    onClick()
                    viewModel.updateTimeTurnOnTurnOffLightInLocal(
                        timeTurnOnLight = selectedTimeTurnOn.value.first.toString() + ":" + selectedTimeTurnOn.value.second.toString(),
                        timeTurnOffLight = selectedTimeTurnOff.value.first.toString() + ":" + selectedTimeTurnOff.value.second.toString(),
                    )
                }

                BodyTextComposable(title = "Auto reset app every day", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemAutoResetAppEveryday, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
                    selectedItemAutoResetAppEveryday = it
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
                    onClick()
                    viewModel.updateAutoResetAppEveryDayInLocal(selectedItemAutoResetAppEveryday.toString())
                }

                BodyTextComposable(title = "Set time reset on everyday", fontWeight = FontWeight.Bold, paddingBottom = 16.dp)
                if(state.initSetup!=null){
                    Logger.info("1hour reset: $hourReset, minute reset: $minuteReset")
                    TimePickerWrapperComposable(
                        defaultHour = state.initSetup.timeResetOnEveryDay.split(":")[0].toInt(),
                        defaultMinute = state.initSetup.timeResetOnEveryDay.split(":")[1].toInt(),
                        onTimeSelected = onTimeSelectedReset
                    )
                } else {
                    Logger.info("2hour reset: $hourReset, minute reset: $minuteReset")
                    TimePickerWrapperComposable(
                        defaultHour = 0,
                        defaultMinute = 0,
                        onTimeSelected = onTimeSelectedReset
                    )
                }
                CustomButtonComposable(
                    title = "SAVE",
                    cornerRadius = 4.dp,
                    titleAlignment = TextAlign.Center,
                    height = 60.dp,
                    paddingTop = 18.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 30.dp,
                ) {
                    onClick()
                    viewModel.saveSetTimeResetOnEveryDay(
                        hour = selectedTimeReset.value.first,
                        minute = selectedTimeReset.value.second,
                    )
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
                    onClick()
                    viewModel.checkDropSensor()
                }

                BodyTextComposable(title = "Drop sensor", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemDropSensor, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
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
                    onClick()
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
                    onClick()
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
                    onClick()
                    viewModel.updateInchingModeInLocal(selectedItemInchingMode.toString())
                }

                BodyTextComposable(title = "Big Ads", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemBigAds, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
                    selectedItemBigAds = it
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
                    onClick()
                    viewModel.updateBigAdsInLocal(selectedItemBigAds.toString())
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
                    onClick()
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
                    onClick()
                    viewModel.updateTimeJumpToAdsScreenInLocal(selectedItemTimeJumpToAdsScreen.toString().substringBefore("s"))
                }

                BodyTextComposable(title = "Glass heating mode", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemGlassHeatingMode, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    onClick()
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
                    onClick()
                    viewModel.updateGlassHeatingModeInLocal(selectedItemGlassHeatingMode.toString())
                }

                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {

                    Column(modifier = Modifier.width(screenWidthDp/2).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {
                        BodyTextComposable(title = "Lowest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                        if(state.initSetup!=null) {
                            EditTextComposable(initText = state.initSetup.lowestTempWarning, keyboardTypeNumber = true) {
                                onClick()
                                inputLowestTempWarning = it
                            }
                        } else {
                            EditTextComposable(initText = "", keyboardTypeNumber = true) {
                                onClick()
                                inputLowestTempWarning = it
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.width(screenWidthDp/2).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }) {
                        BodyTextComposable(title = "Highest temperature warning", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                        if(state.initSetup!=null) {
                            EditTextComposable(initText = state.initSetup.highestTempWarning, keyboardTypeNumber = true) {
                                onClick()
                                inputHighestTempWarning = it
                            }
                        } else {
                            EditTextComposable(initText = "", keyboardTypeNumber = true) {
                                onClick()
                                inputHighestTempWarning = it
                            }
                        }
                    }
                }
                CustomButtonComposable(
                    title = "SAVE",
                    cornerRadius = 4.dp,
                    titleAlignment = TextAlign.Center,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    onClick()
                    viewModel.updateHighestAndLowestTempWarningInLocal(inputHighestTempWarning, inputLowestTempWarning)
                }

                BodyTextComposable(title = "Temperature", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                if(state.initSetup!=null) {
                    TitleAndEditTextComposable(
                        title = "",
                        paddingBottom = 12.dp,
                        initText = state.initSetup.temperature,
                        keyboardTypeNumber = true
                    ) {
                        onClick()
                        inputTemperature = it
                    }
                } else {
                    TitleAndEditTextComposable(
                        title = "",
                        paddingBottom = 12.dp,
                        initText = "",
                        keyboardTypeNumber = true
                    ) {
                        onClick()
                        inputTemperature = it
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
                    onClick()
                    keyboardController?.hide()
                    viewModel.updateTemperatureInLocal(inputTemperature)
                }


                BodyTextComposable(title = "Temperature 1: ${state.temp1}${if(state.temp1.isNotEmpty()&&state.temp1!="không thể kết nối")"℃" else ""}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                BodyTextComposable(title = "Temperature 2: ${state.temp2}${if(state.temp2.isNotEmpty()&&state.temp2!="không thể kết nối")"℃" else ""}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                CustomButtonComposable(
                    title = "READ TEMPERATURE",
                    wrap = true,
                    cornerRadius = 4.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    onClick()
                    viewModel.getTemp()
                }

                CustomButtonComposable(
                    title = "ON LIGHT",
                    wrap = true,
                    cornerRadius = 4.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    onClick()
                    viewModel.onLight()
                }
                CustomButtonComposable(
                    title = "OFF LIGHT",
                    wrap = true,
                    cornerRadius = 4.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    onClick()
                    viewModel.offLight()
                }
            } else {
            CustomButtonComposable(
                title = "ON LIGHT",
                wrap = true,
                cornerRadius = 4.dp,
                height = 60.dp,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                paddingBottom = 50.dp,
            ) {
                onClick()
                viewModel.onLight()
            }
            CustomButtonComposable(
                title = "OFF LIGHT",
                wrap = true,
                cornerRadius = 4.dp,
                height = 60.dp,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                paddingBottom = 50.dp,
            ) {
                onClick()
                viewModel.offLight()
            }
                }
            }


//        CustomButtonComposable(
//            title = "ON LIGHT",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            onClick()
//            viewModel.onLight()
//        }
//        CustomButtonComposable(
//            title = "OFF LIGHT",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            onClick()
//            viewModel.offLight()
//        }
////        CustomButtonComposable(
////            title = "RESET FACTORY",
////            cornerRadius = 4.dp,
////            height = 60.dp,
////            fontWeight = FontWeight.Bold,
////            titleAlignment = TextAlign.Center,
////            fontSize = 20.sp,
////            paddingBottom = 20.dp,
////        ) {
////            viewModel.showDialogConfirm("Are you sure to reset factory?", null, "resetFactory")
////        }
//
//        CustomButtonComposable(
//            title = "REFRESH",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            onClick()
//            val randomSN = Random().nextInt(256).toByte()
//
//            // Define the LED status (0 for off, 1 for on)
//            val ledStatus = 1 // Change this to 0 for turning off the LED
//
//            // Call the turnOnLed function with the generated SN and LED status
////            turnOnLed()
//            viewModel.turnOnLed()
//        }

//        CustomButtonComposable(
//            title = "ON",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            onClick()
//            viewModel.check1()
//        }
//        CustomButtonComposable(
//            title = "OFF",
//            wrap = true,
//            cornerRadius = 4.dp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//            paddingBottom = 50.dp,
//        ) {
//            onClick()
//            viewModel.check2()
//        }
    }
}