package com.combros.vendingmachine.features.home.presentation.screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.combros.vendingmachine.R
import com.combros.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.combros.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.combros.vendingmachine.core.util.pathFolderImagePayment
import com.combros.vendingmachine.core.util.pathFolderImageProduct
import com.combros.vendingmachine.core.util.toVietNamDong
import com.combros.vendingmachine.features.home.presentation.composables.AdsHomeComposable
import com.combros.vendingmachine.features.home.presentation.composables.BackgroundHomeComposable
import com.combros.vendingmachine.features.home.presentation.composables.BigAdsComposable
import com.combros.vendingmachine.features.home.presentation.composables.DatetimeHomeComposable
import com.combros.vendingmachine.features.home.presentation.composables.InformationHomeComposable
import com.combros.vendingmachine.features.home.presentation.composables.PutMoneyComposable
import com.combros.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.combros.vendingmachine.features.home.presentation.viewState.HomeViewState
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
internal fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val localStorageDatasource = LocalStorageDatasource()
    LaunchedEffect(Unit) {
        viewModel.loadInitData()
        while (true) {
            delay(1000)
            viewModel.pollStatus()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if(!state.isVendingMachineBusy) {
                viewModel.readDoor()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            if(!state.isVendingMachineBusy) {
                viewModel.pushLogToServer()
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.closePort()
        }
    }

    HomeContent(
        context = context,
        state = state,
        viewModel = viewModel,
        navController = navController,
        localStorageDatasource = localStorageDatasource,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeContent(
    context: Context,
    state: HomeViewState,
    viewModel: HomeViewModel,
    navController: NavHostController,
    localStorageDatasource: LocalStorageDatasource,
) {
    var checkTouch by remember { mutableLongStateOf(0) }
    val focusManager = LocalFocusManager.current
    // Capture any interaction on the screen
    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            checkTouch = 0
        }
    }
    if(state.initSetup!=null) {
        if(!state.isShowBigAds && !state.isShowWaitForDropProduct) {
            checkTouch = 0
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000L)
                    checkTouch++
//                    Logger.debug("check touch = $checkTouch, ${state.initSetup.timeoutJumpToBigAdsScreen.toLong()}")
                    if(state.initSetup.fullScreenAds == "ON") {
                        if(checkTouch>state.initSetup.timeoutJumpToBigAdsScreen.toLong() && state.listBigAds.isNotEmpty()) {
//                        Logger.debug("vo check touch")
                            viewModel.showBigAds()
                            break
                        }
                    }
                }
            }
        }
    }
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = { viewModel.hideDialogWarning() },
    )
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = { viewModel.hideDialogConfirm() },
        onClickConfirm = { viewModel.hideDialogConfirm() },
    )
    Scaffold(modifier = interactionModifier) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            BackgroundHomeComposable()
            Column(modifier = Modifier.fillMaxSize()) {
                if(state.listAds.isNotEmpty()) {
                    if(state.isShowAds && !state.isShowBigAds) {
                        AdsHomeComposable(
                            context = context,
                            listAds = state.listAds,
                            onClickHideAds = { viewModel.hideAdsDebounced() },
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .height(400.dp)
                            .background(Color.Black)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {

                    }
                }
                if(state.initSetup!=null) {
                    DatetimeHomeComposable(
                        temp1 = state.temp1,
                        temp2 = state.temp2,
                        getTempStatusNetworkAndPower = {
                            viewModel.getTemp()
                            viewModel.writeLogStatusNetworkAndPower()
                        })
                } else {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFA31412)),
                        Arrangement.Center,
                        Alignment.CenterVertically,
                    ) {
                        Text(
                            "Nhiệt độ: ...",
                            modifier = Modifier.padding(start = 6.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "00:00 - 00 Tháng 00, 2000",
                            modifier = Modifier.padding(end = 6.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }
                }
                if(state.initSetup!=null) {
                    InformationHomeComposable(navController = navController, vendCode = state.initSetup!!.vendCode)
                } else {
                    Row(
                        modifier = Modifier
                            .height(80.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFCB1A17)),
                        Arrangement.Center,
                        Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                Arrangement.Center,
                                Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    Arrangement.Center,
                                    Alignment.CenterVertically,
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .width(21.dp)
                                            .height(21.dp)
                                            .clickable { },
                                        alignment = Alignment.TopEnd,
                                        painter = painterResource(id = R.drawable.image_circle_phone),
                                        contentDescription = ""
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "1900.99.99.80", color = Color.White, fontSize = 15.sp)
                                }
                                Text(text = "AVF000000", color = Color.White, fontSize = 13.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            Alignment.Center,
                        ) {
                            Image(
                                modifier = Modifier
                                    .height(26.dp),
                                alignment = Alignment.Center,
                                painter = painterResource(id = R.drawable.image_logo_avf),
                                contentDescription = ""
                            )
                        }
                        Box(modifier = Modifier
                            .weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                Arrangement.Center,
                                Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.height(20.dp),
                                    alignment = Alignment.Center,
                                    painter = painterResource(id = R.drawable.image_flags),
                                    contentDescription = ""
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Tiếng việt", color = Color.White)
                            }
                        }
                    }
                }
                if(state.initSetup!=null) {
                    Box(modifier = Modifier.fillMaxHeight()) {
                        val chunks = state.listSlotInHome.chunked(state.initSetup.layoutHomeScreen.toInt())
                        Column(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(54.dp))
                            chunks.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowItems.forEach { slot ->
//                                    var isChoose by remember { mutableStateOf(false) }
//                                    var numberProduct by remember { mutableIntStateOf(0) }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(
                                                    width = 0.dp,
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(22.dp)
                                                )
                                                .background(
                                                    Color.White,
                                                    shape = RoundedCornerShape(22.dp)
                                                ),
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .padding(20.dp)
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                val imageModifier = Modifier
                                                    .width(150.dp)
                                                    .height(150.dp)
                                                val imagePainter = if (slot.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                                        pathFolderImageProduct + "/${slot.productCode}.png"
                                                    )
                                                ) {
                                                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                                                        .data(pathFolderImageProduct + "/${slot.productCode}.png")
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

                                                Spacer(modifier = Modifier.height(20.dp))

                                                Text(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 10.dp),
                                                    text = slot.productName,
                                                    minLines = 2,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 17.sp,
                                                )

                                                BodyTextComposable(
                                                    title = slot.price.toVietNamDong(),
                                                    fontSize = 19.sp,
                                                    paddingBottom = 20.dp,
                                                    color = Color(0xFFE72B28),
                                                    fontWeight = FontWeight.Bold,
                                                )

                                                if (state.listSlotInCard.isNotEmpty() && viewModel.getInventoryByProductCode(slot.productCode) != -1) {
                                                    Box(
                                                        modifier = Modifier
                                                            .height(60.dp)
                                                            .border(
                                                                width = 0.dp,
                                                                color = Color(0xFFE72B28),
                                                                shape = RoundedCornerShape(50.dp)
                                                            ),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(horizontal = 20.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            Image(
                                                                modifier = Modifier
                                                                    .height(30.dp)
                                                                    .width(30.dp)
                                                                    .clickable {
                                                                        viewModel.minusProductDebounced(
                                                                            slot
                                                                        )
                                                                    },
                                                                alignment = Alignment.Center,
                                                                painter = painterResource(id = R.drawable.image_minus),
                                                                contentDescription = ""
                                                            )
                                                            Text(
                                                                "${viewModel.getInventoryByProductCode(slot.productCode)}",
                                                                fontSize = 19.sp,
                                                            )
                                                            Image(
                                                                modifier = Modifier
                                                                    .height(30.dp)
                                                                    .width(30.dp)
                                                                    .clickable {
                                                                        viewModel.plusProductDebounced(
                                                                            slot
                                                                        )
                                                                    },
                                                                alignment = Alignment.Center,
                                                                painter = painterResource(id = R.drawable.image_plus),
                                                                contentDescription = ""
                                                            )
                                                        }
                                                    }
                                                } else {
                                                    Button(
                                                        onClick = {
                                                            viewModel.addProductDebounced(slot)
                                                        },
                                                        modifier = Modifier
                                                            .height(60.dp)
                                                            .border(
                                                                width = 0.dp,
                                                                color = Color(0xFFE72B28),
                                                                shape = RoundedCornerShape(50.dp)
                                                            ),
                                                        colors = ButtonDefaults.buttonColors(
                                                            Color(0xFFE72B28),
                                                            contentColor = Color.Black
                                                        ),
                                                        shape = RoundedCornerShape(50.dp),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            Arrangement.Center,
                                                            Alignment.CenterVertically,
                                                        ) {
                                                            Image(
                                                                modifier = Modifier
                                                                    .padding(end = 6.dp)
                                                                    .height(30.dp),
                                                                alignment = Alignment.Center,
                                                                painter = painterResource(id = R.drawable.image_select_to_buy),
                                                                contentDescription = ""
                                                            )
                                                            Text("Chọn mua", color = Color.White, fontSize = 18.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Add empty slots to fill the row if the row is not complete
                                    repeat(3 - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Spacer(modifier = Modifier.height(120.dp))
                        }
                        Row {
                            Box(
                                modifier = Modifier
                                    .padding(top = 14.dp)
                                    .background(
                                        Color(0xFFF59E0B),
                                        shape = RoundedCornerShape(
                                            topEnd = 50.dp,
                                            bottomEnd = 50.dp
                                        )
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .height(80.dp),
                                    Arrangement.Center,
                                    Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier

                                    ) {
                                        Text("Số dư tiền mặt", fontSize = 15.sp, color = Color.White)
                                        Text(state.initSetup.currentCash.toVietNamDong(), fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(
                                        modifier = Modifier.padding(end = 10.dp).clickable {
                                            checkTouch = 0
                                            if (state.initSetup.withdrawalAllowed == "ON") {
                                                viewModel.withdrawalMoney()
                                            }
                                        },
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .padding(bottom = 2.dp, top = 2.dp)
                                                .height(28.dp)
                                                .width(28.dp),
                                            alignment = Alignment.Center,
                                            painter = painterResource(id = R.drawable.image_withdraw),
                                            contentDescription = ""
                                        )
                                        Text(
                                            "Hoàn Tiền",
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if(!state.isShowAds) {
                                Button(
                                    modifier = Modifier
                                        .padding(end = 14.dp, top = 14.dp)
                                        .background(
                                            color = Color(0xFF9CA3AF),
                                            shape = RoundedCornerShape(4.dp) // Set the shape here
                                        ),
                                    colors = ButtonDefaults.buttonColors(
                                        Color(0xFF9CA3AF), // Button background color
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(4.dp), // Set the shape again for consistency
                                    onClick = { viewModel.showAdsDebounced() },
                                ) {
                                    Text(
                                        text = "Bật quảng cáo",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                    )
                                }
                            }
                        }
                        if(state.listSlotInCard.isNotEmpty()) {
                            Row (
                                modifier = Modifier
                                    .height(120.dp)
                                    .background(Color.White)
                                    .padding(horizontal = 20.dp)
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val imageModifier = Modifier
                                    .width(80.dp)
                                    .height(80.dp)
                                val imagePainter = if (state.slotAtBottom!!.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                        pathFolderImageProduct +"/${state.slotAtBottom.productCode}.png")) {
                                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                                        .data(pathFolderImageProduct +"/${state.slotAtBottom.productCode}.png")
                                        .build()
                                    rememberAsyncImagePainter(imageRequest)
                                } else {
                                    painterResource(id = R.drawable.image_add_slot)
                                }
                                Image(
                                    modifier = imageModifier,
                                    painter = imagePainter,
                                    contentDescription = ""
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column() {
                                    Text(state.slotAtBottom.productName)
                                    Text("(Số lượng ${state.slotAtBottom.inventory})")
                                    Text(state.slotAtBottom.price.toVietNamDong(), color = Color(0xFFE72B28), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Button(
                                    onClick = { viewModel.showPaymentDebounced() },
                                    modifier = Modifier
                                        .height(80.dp)
                                        .wrapContentWidth()
                                        .border(
                                            width = 0.dp,
                                            color = Color(0xFFE72B28),
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(
                                        Color(0xFFE72B28),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        Arrangement.Center,
                                        Alignment.CenterVertically,
                                    ) {
                                        Text("Thanh toán", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                        Text(" | ", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                        Text(viewModel.getTotal().toVietNamDong(), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(state.isShowCart && state.listSlotInCard.isNotEmpty()) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable { }) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .border(
                                width = 0.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            )
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 22.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            ),
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            CustomButtonComposable(
                                title = "Quay lại",
                                wrap = true,
                                paddingBottom = 36.dp,
                                fontSize = 20.sp,
                                height = 60.dp,
                                fontWeight = FontWeight.Bold,
                                cornerRadius = 6.dp,
                            ) {
                                viewModel.backDebounced()
                            }
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 400.dp)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                state.listSlotInCard.forEach { item ->
                                    Row (
                                        modifier = Modifier.padding(bottom = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val imageModifier = Modifier
                                            .width(130.dp)
                                            .height(130.dp)
                                            .padding(end = 10.dp)
                                        val imagePainter = if (item.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                                pathFolderImageProduct + "/${item.productCode}.png"
                                            )
                                        ) {
                                            val imageRequest = ImageRequest.Builder(LocalContext.current)
                                                .data(pathFolderImageProduct + "/${item.productCode}.png")
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

                                        Column() {
                                            Text(
                                                item.productName,
                                                fontSize = 19.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Đơn giá: ${item.price.toVietNamDong()}", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(50.dp)
                                                        .height(40.dp)
                                                        .border(
                                                            width = 0.2.dp,
                                                            color = Color.Gray,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .background(
                                                            Color.White,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.minusProductDebounced(
                                                                item
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center,                                            ) {
                                                    Image(
                                                        modifier = Modifier
                                                            .height(20.dp)
                                                            .width(20.dp),
                                                        alignment = Alignment.Center,
                                                        painter = painterResource(id = R.drawable.image_minus),
                                                        contentDescription = ""
                                                    )
                                                }

                                                Text(
                                                    "${item.inventory}",
                                                    fontSize = 19.sp,
                                                    modifier = Modifier.padding(horizontal = 20.dp)
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(50.dp)
                                                        .height(40.dp)
                                                        .border(
                                                            width = 0.2.dp,
                                                            color = Color.Gray,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .background(
                                                            Color.White,
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.plusProductDebounced(
                                                                item
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Image(
                                                        modifier = Modifier
                                                            .height(20.dp)
                                                            .width(20.dp),
                                                        alignment = Alignment.Center,
                                                        painter = painterResource(id = R.drawable.image_plus),
                                                        contentDescription = ""
                                                    )
                                                }

                                                Spacer(modifier = Modifier.weight(1f))
                                                Text((item.price*item.inventory).toVietNamDong(), color = Color(0xFFE72B28), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }


                            }
                            Row(
                                modifier = Modifier
                                    .padding(top = 28.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                var text by remember { mutableStateOf("") }
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .height(60.dp)
                                        .weight(1f),
                                    placeholder = { Text(text = "Mã giảm giá", fontSize = 18.sp) }, // Hint
                                    shape = RoundedCornerShape(4.dp),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
                                    colors = outlinedTextFieldColors(
                                        focusedBorderColor = Color.Gray,
                                        unfocusedBorderColor = Color.Gray,
                                    ),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    visualTransformation = VisualTransformation.None,
                                    keyboardActions = KeyboardActions (
                                        onDone = {
//                                            keyboardControllerNumberSlot?.hide()
                                            focusManager.clearFocus()
                                        }
                                    )

                                )
                                CustomButtonComposable(
                                    title = "Áp dụng",
                                    cornerRadius = 6.dp,
                                    fontSize = 20.sp,
                                    wrap = true,
                                    height = 60.dp,
                                    fontWeight = FontWeight.Bold,
                                ) {
                                    viewModel.applyPromotionDebounced(text)
                                }
                            }
                            Spacer(modifier = Modifier.height(34.dp))
                            Row {
                                Text(
                                    "Khuyến mãi",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 20.sp,
                                )
                                Text(
                                    if(state.promotion == null) 0.toVietNamDong()
                                    else state.promotion.totalDiscount!!.toVietNamDong()
                                    , fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            Row {
                                Text(
                                    "Tổng tiền thanh toán",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 20.sp,
                                )
                                Text(state.totalAmount.toVietNamDong(), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            Row {
                                Text(
                                    "Số dư tiền mặt trên máy",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 20.sp,
                                )
                                Text(if(state.initSetup!=null) state.initSetup.currentCash.toVietNamDong() else "0vnđ", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(28.dp))
                            Text(
                                "Hình thức thanh toán",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                            )
                            if(state.listPaymentMethod.isEmpty()) {
                                Text(
                                    "Hiện không có phương thức thanh toán nào khả dụng! Xin vui lòng thử lại sau!",
                                    modifier = Modifier
                                        .padding(top = 113.dp, bottom = 103.dp)
                                        .fillMaxWidth(),
                                    color = Color.Red,
                                    textAlign = TextAlign.Center,
                                )
                            } else {
                                Box(modifier = Modifier) {
                                    val chunks = state.listPaymentMethod.chunked(3)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 28.dp)
                                    ) {
                                        chunks.forEach { rowItems ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                rowItems.forEach { item ->
//                                            var isChoose by remember { mutableStateOf(false) }
//                                            if(item.methodName == "cash") {
//                                                isChoose = true
//                                            }
//                                            var nameMethodPayment by remember { mutableStateOf("cash") }
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(106.dp)
                                                            .padding(bottom = 14.dp)
                                                            .border(
                                                                width = if (state.nameMethodPayment == item.methodName) 2.dp else 0.4.dp,
                                                                color = if (state.nameMethodPayment == item.methodName) Color.Green else Color.Gray,
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .background(
                                                                Color.White,
                                                                shape = RoundedCornerShape(6.dp)
                                                            )
                                                            .clickable {
                                                                viewModel.updateNameMethod(item.methodName!!)
                                                            },
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .padding(start = 24.dp, end = 10.dp)
                                                                .fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                        ) {
                                                            val imageModifier = Modifier
                                                                .width(44.dp)
                                                                .height(44.dp)
                                                                .clickable { }
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
                                                            Text(
                                                                item.brief ?: "",
                                                                modifier = Modifier.padding(20.dp),
                                                                fontSize = 16.sp,
                                                                maxLines = 2,
                                                                overflow = TextOverflow.Ellipsis,
                                                            )
                                                        }
                                                    }
                                                    // Add spacers to fill up the row if there are less than 3 items
                                                    if (rowItems.size < 3) {
                                                        repeat(3 - rowItems.size) {
                                                            Spacer(modifier = Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if(state.listPaymentMethod.isNotEmpty()) {
                                CustomButtonComposable(
                                    title = "Xác nhận thanh toán",
                                    cornerRadius = 6.dp,
                                    titleAlignment = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    height = 70.dp,
                                    fontSize = 20.sp,
                                    paddingTop = 12.dp,
                                ) {
                                    viewModel.paymentConfirmation()
                                }
                            }
                        }
                    }
                }
            }
            if(state.isShowPushMoney) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .border(
                            width = 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp
                            )
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 22.dp,
                                topEnd = 22.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp
                            )
                        ),
                ) {
                    PutMoneyComposable(
                        initSetup = state.initSetup!!,
                        countDownPaymentByCash = state.countDownPaymentByCash,
                        totalAmount = state.totalAmount,
                        onClickChooseAnotherMethodPayment = { viewModel.chooseAnotherMethodPayment() },
                        onClickBackInPayment = { viewModel.backInPayment() }
                    )
                }
            }
            if(state.isShowQrCode) {
//                Logger.debug("adsfasdffgwrefrgsfgasdfsdfsdfdsffddff")
//                Box(
//                    modifier = Modifier
//                        .height(200.dp)
//                        .align(Alignment.BottomCenter)
//                        .border(
//                            width = 0.dp,
//                            color = Color.White,
//                            shape = RoundedCornerShape(
//                                topStart = 22.dp,
//                                topEnd = 22.dp,
//                                bottomEnd = 0.dp,
//                                bottomStart = 0.dp
//                            )
//                        )
//                        .background(
//                            color = Color.White,
//                            shape = RoundedCornerShape(
//                                topStart = 22.dp,
//                                topEnd = 22.dp,
//                                bottomEnd = 0.dp,
//                                bottomStart = 0.dp
//                            )
//                        ),
//                ) {
//                    Image(
//                        modifier = Modifier
//                            .padding(bottom = 20.dp)
//                            .height(300.dp)
//                            .width(300.dp),
//                        alignment = Alignment.Center,
//                        painter = painterResource(id = R.drawable.image_get_product),
//                        contentDescription = ""
//                    )
////                    Text("dfsdfsdfdff")
//                }
                Dialog(
                    onDismissRequest = { /*TODO*/ },
                    properties = DialogProperties(dismissOnClickOutside = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(630.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 17.dp, vertical = 17.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                modifier = Modifier
                                    .clickable { viewModel.hideShowQrCode() }
                                    .align(Alignment.End)
                                    .height(38.dp)
                                    .width(38.dp),
                                alignment = Alignment.Center,
                                painter = painterResource(id = R.drawable.image_close),
                                contentDescription = ""
                            )
                            Text(
                                "Thanh toán ${state.nameMethodPayment}",
                                modifier = Modifier.padding(bottom = 4.dp, top = 4.dp),
                                fontSize = 21.sp,
                            )
                            Image(
                                bitmap = state.imageBitmap!!,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(460.dp)
                            )
                            Text(
                                "Vui lòng quét mã trong ${state.countDownPaymentByOnline}s",
                                modifier = Modifier.padding(bottom = 30.dp, top = 10.dp),
                                fontSize = 21.sp,
                            )
                        }
                    }
                }
            }
            if(state.isShowBigAds) {
                BigAdsComposable(
                    context = context,
                    listAds = state.listAds,
                    onClickHideAds = {
//                        updateInteractionTime()
                        viewModel.hideBigAds()
                    }
                )
            }
            if(state.isShowWaitForDropProduct) {
                Dialog(
                    onDismissRequest = { /*TODO*/ },
                    properties = DialogProperties(dismissOnClickOutside = false)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(560.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                modifier = Modifier
                                    .padding(bottom = 20.dp)
                                    .height(300.dp)
                                    .width(300.dp),
                                alignment = Alignment.Center,
                                painter = painterResource(id = R.drawable.image_get_product),
                                contentDescription = ""
                            )
                            Text(
                                "Vui lòng chờ để nhận sản phẩm từ khe bên dưới",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 21.sp,
                            )
                            Text(
                                "Nước có ga vui lòng mở sau 2 phút",
                                modifier = Modifier.padding(bottom = 36.dp),
                                fontSize = 21.sp,
                            )
                            Text(
                                "Nếu bạn cần hỗ trợ thêm liên hệ hotline",
                                modifier = Modifier.padding(bottom = 10.dp),
                                fontSize = 21.sp,
                            )
                            Text(
                                "1900.99.99.89",
                                modifier = Modifier.padding(bottom = 34.dp),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE72B28),
                            )
                        }
                    }
                }
            }
//            if(state.isShowDropFail) {
//                Dialog(
//                    onDismissRequest = { /*TODO*/ },
//                    properties = DialogProperties(dismissOnClickOutside = false)
//                ) {
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        color = Color.White,
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(30.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center,
//                        ) {
//                            Text(
//                                "Có ${state.numberProductDroppedFail} sản phẩm rớt không thành công! Vui lòng mua sản phẩm khác hoặc rút lại tiền thừa",
//                                modifier = Modifier.padding(bottom = 32.dp),
//                                lineHeight = 30.sp,
//                                textAlign = TextAlign.Center,
//                                fontSize = 22.sp,
//                            )
//                            Row {
//                                CustomButtonComposable(
//                                    title = "Mua sản phẩm khác",
//                                    height = 65.dp,
//                                    fontWeight = FontWeight.Bold,
//                                    paddingEnd = 5.dp,
//                                    cornerRadius = 6.dp,
//                                    fontSize = 20.sp,
//                                    wrap = true,
//                                ) {
//                                    viewModel.hideDropFailDebounced()
//                                }
//                                CustomButtonComposable(
//                                    title = "Rút tiền thừa",
//                                    fontWeight = FontWeight.Bold,
//                                    height = 65.dp,
//                                    paddingStart = 5.dp,
//                                    cornerRadius = 6.dp,
//                                    fontSize = 20.sp,
//                                    wrap = true,
//                                ) {
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}