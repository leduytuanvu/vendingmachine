package com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TimePickerWrapperComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFolderImagePayment
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewModel.SetupPaymentViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.viewState.SetupPaymentViewState

@Composable
internal fun SetupPaymentScreen(
    navController: NavHostController,
    viewModel: SetupPaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadInitData()
    }
    SetupPaymentContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupPaymentContent(
    state: SetupPaymentViewState,
    viewModel: SetupPaymentViewModel,
    navController: NavHostController,
) {
    val localStorageDatasource = LocalStorageDatasource()

    var selectedItemDefaultPromotion by remember {
        mutableStateOf(AnnotatedString("ON"))
    }
    var selectedItemTimeOutPaymentQrCode by remember {
        mutableStateOf(AnnotatedString("30s"))
    }

    var selectedItemTimeOutPaymentCash by remember {
        mutableStateOf(AnnotatedString("30s"))
    }

    val selectedTimeReset = remember { mutableStateOf<Pair<Int, Int>>(Pair(0, 0)) }
    val onTimeSelectedReset: (Int, Int) -> Unit = { hour, minute ->
        selectedTimeReset.value = Pair(hour, minute)
    }
    var partsReset by remember {
        mutableStateOf(if (state.initSetup != null) state.initSetup.timeTurnOnLight.split(":") else listOf("0", "0"))
    }
    var hourReset by remember { mutableIntStateOf(partsReset[0].toIntOrNull() ?: 0) }
    var minuteReset by remember { mutableIntStateOf(partsReset.getOrNull(1)?.toIntOrNull() ?: 0) }

    LaunchedEffect(state.initSetup) {
        selectedItemDefaultPromotion = AnnotatedString(state.initSetup?.initPromotion ?: "ON")
        selectedItemTimeOutPaymentQrCode = AnnotatedString(if(state.initSetup?.timeoutPaymentByQrCode != null) "${state.initSetup.timeoutPaymentByQrCode}s" else "30s")
        selectedItemTimeOutPaymentCash = AnnotatedString(if(state.initSetup?.timeoutPaymentByCash != null) "${state.initSetup.timeoutPaymentByCash}s" else "30s")

        partsReset = if (state.initSetup?.timeResetOnEveryDay != null) state.initSetup.timeResetOnEveryDay.split(":") else listOf("0", "0")
        hourReset = partsReset[0].toIntOrNull() ?: 0
        minuteReset = partsReset.getOrNull(1)?.toIntOrNull() ?: 0
    }

    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = { viewModel.hideDialogWarning() },
    )
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 102.dp)
                .verticalScroll(rememberScrollState()),
            content = {
                Spacer(modifier = Modifier.height(50.dp))

                BodyTextComposable(
                    title = "Current cash: ${if(state.initSetup == null) 0.toVietNamDong() else state.initSetup.currentCash.toVietNamDong()}",
                    fontWeight = FontWeight.Bold,
                    paddingBottom = 8.dp,
                )
                CustomButtonComposable(
                    title = "REFRESH",
                    wrap = true,
                    cornerRadius = 4.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    viewModel.refreshCurrentCash()
                }

                BodyTextComposable(title = "Rotten box balance: ${state.numberRottenBoxBalance}", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                CustomButtonComposable(
                    title = "REFRESH",
                    wrap = true,
                    cornerRadius = 4.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    viewModel.refreshRottenBoxBalance()
                }

                BodyTextComposable(title = "Default promotion", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
                TitleAndDropdownComposable(title = "", items = listOf(
                    AnnotatedString("ON"),
                    AnnotatedString("OFF"),
                ), selectedItem = selectedItemDefaultPromotion, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    selectedItemDefaultPromotion = it
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
                    viewModel.saveDefaultPromotion(selectedItemDefaultPromotion.toString())
                }

                BodyTextComposable(title = "Time out payment online", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
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
                ), selectedItem = selectedItemTimeOutPaymentQrCode, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    selectedItemTimeOutPaymentQrCode = it
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
                    viewModel.saveTimeoutPaymentQrCode(selectedItemTimeOutPaymentQrCode.toString())
                }

                BodyTextComposable(title = "Time out payment cash", fontWeight = FontWeight.Bold, paddingBottom = 8.dp)
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
                ), selectedItem = selectedItemTimeOutPaymentCash, paddingTop = 2.dp, paddingBottom = 12.dp) {
                    selectedItemTimeOutPaymentCash = it
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
                    viewModel.saveTimeoutPaymentCash(selectedItemTimeOutPaymentCash.toString())
                }

                BodyTextComposable(title = "Method payment", fontWeight = FontWeight.Bold, paddingBottom = 18.dp)
                for(item in state.listPaymentMethod) {
                    Row(
                        modifier = Modifier.padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        val imageModifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                        val imagePainter = if (item.methodName!!.isNotEmpty() && localStorageDatasource.checkFileExists(
                                pathFolderImagePayment + "/${item.methodName}.png"
                            )
                        ) {
                            val imageRequest = ImageRequest.Builder(LocalContext.current)
                                .data(pathFolderImagePayment + "/${item.methodName}.png")
                                .build()
                            rememberAsyncImagePainter(imageRequest)
                        } else {
                            painterResource(id = R.drawable.image_error)
                        }
                        Image(
                            modifier = imageModifier,
                            painter = imagePainter,
                            contentDescription = ""
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(item.brief!!, fontSize = 18.sp)
                    }
                }
                CustomButtonComposable(
                    title = "DOWNLOAD PAYMENT METHODS",
                    wrap = true,
                    cornerRadius = 4.dp,
                    paddingTop = 14.dp,
                    height = 60.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    paddingBottom = 50.dp,
                ) {
                    viewModel.downloadListMethodPayment()
                }

                BodyTextComposable(title = "Set time reset on everyday", fontWeight = FontWeight.Bold, paddingBottom = 16.dp)
                if(state.initSetup!=null){
                    TimePickerWrapperComposable(
                        defaultHour = hourReset,
                        defaultMinute = minuteReset,
                        onTimeSelected = onTimeSelectedReset
                    )
                } else {
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
                    viewModel.saveSetTimeResetOnEveryDay(
                        hour = selectedTimeReset.value.first,
                        minute = selectedTimeReset.value.second,
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)) {
            CustomButtonComposable(
                title = "BACK",
                wrap = true,
                height = 70.dp,
                fontSize = 20.sp,
                cornerRadius = 4.dp,
                paddingTop = 20.dp,
                paddingStart = 20.dp,
                paddingBottom = 20.dp,
                fontWeight = FontWeight.Bold,
            ) { navController.popBackStack() }
        }
    }
}