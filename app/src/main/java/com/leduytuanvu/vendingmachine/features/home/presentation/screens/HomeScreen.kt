package com.leduytuanvu.vendingmachine.features.home.presentation.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.VideoView
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.getCurrentDateTime
import com.leduytuanvu.vendingmachine.core.util.pathFolderImagePayment
import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.AdsHomeComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.BackgroundHomeComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.BigAdsComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.DatetimeHomeComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.InformationHomeComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.composables.PaymentConfirmComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState
import kotlinx.coroutines.delay

@Composable
internal fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val localStorageDatasource = LocalStorageDatasource()
    val lifecycleOwner = LocalLifecycleOwner.current
    // Register the lifecycle observer
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.onStop()
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
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Update last interaction time on any interaction
    val updateInteractionTime = {
        lastInteractionTime = System.currentTimeMillis()
    }

    // Capture any interaction on the screen
    val interactionModifier = Modifier.pointerInput(Unit) {
        detectTapGestures {
            updateInteractionTime()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (System.currentTimeMillis() - lastInteractionTime > (state.initSetup!!.timeoutJumpToBigAdsScreen.toLong()*1000)) { // 60 seconds
                if (!state.isShowBigAds) {
                    viewModel.showBigAds()
                } else {
                    updateInteractionTime()
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            viewModel.pollStatus()
        }
    }
    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(modifier = interactionModifier) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            BackgroundHomeComposable()
            Column(modifier = Modifier.fillMaxSize()) {
                if(state.isShowAds && !state.isShowBigAds) {
                    AdsHomeComposable(
                        context = context,
                        listAds = state.listAds,
                        onClickHideAds = { viewModel.hideAdsDebounced() },
                    )
                }
                DatetimeHomeComposable()
                InformationHomeComposable(navController = navController, vendCode = state.initSetup!!.vendCode)
//                ListProductHomeComposable(
//                    listSlotShowInHome = state.listSlotShowInHome,
//                    listSlotInCart = state.listSlotInCart,
//                    slotShowBottom = state.slot,
////                    numberProduct = state.numberProduct,
//                    isShowAds = state.isShowAds,
//                    localStorageDatasource = localStorageDatasource,
//                    onClickMinusProduct = { slot -> viewModel.minusProduct(slot) },
//                    onClickPlusProduct = { slot -> viewModel.plusProduct(slot) },
//                    onClickAddProduct = { slot -> viewModel.addProduct(slot) },
//                    onClickShowAds = { viewModel.showAds() }
//                ) {}
                Box(modifier = Modifier.fillMaxHeight()) {
                    val chunks = state.listSlotInHome.chunked(3)
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
                                                .clickable { }
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
                                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
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
                                Image(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .width(44.dp)
                                        .clickable { },
                                    alignment = Alignment.Center,
                                    painter = painterResource(id = R.drawable.image_withdraw),
                                    contentDescription = ""
                                )
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
                                Text(if(state.slotAtBottom != null) state.slotAtBottom.productName else "")
                                Text(if(state.slotAtBottom!=null) "(Số lượng ${state.slotAtBottom.inventory})" else "")
                                Text(if(state.slotAtBottom!=null) state.slotAtBottom.price.toVietNamDong() else "", color = Color(0xFFE72B28), fontWeight = FontWeight.Bold)
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
            if(state.isPayment && state.listSlotInCard.isNotEmpty()) {
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
                                        .clickable { }
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
                                        Text("Đơn giá: ${item.price.toVietNamDong()}/lon", fontSize = 14.sp)
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
                                                    ),
                                                contentAlignment = Alignment.Center,                                            ) {
                                                Image(
                                                    modifier = Modifier
                                                        .height(20.dp)
                                                        .width(20.dp)
                                                        .clickable {
                                                            viewModel.minusProductDebounced(item)
                                                        },
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
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Image(
                                                    modifier = Modifier
                                                        .height(20.dp)
                                                        .width(20.dp)
                                                        .clickable {
                                                            viewModel.plusProductDebounced(item)
                                                        },
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
                                viewModel.applyPromotion(text)
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
                            Text(
                                if(state.promotion == null) state.totalAmount.toVietNamDong()
                                else state.promotion.paymentAmount!!.toVietNamDong()
                                , fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Row {
                            Text(
                                "Số dư tiền mặt trên máy",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                fontSize = 20.sp,
                            )
                            Text("0vnd", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(28.dp))
                        Text(
                            "Hình thức thanh toán",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
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
            if(state.isPaymentConfirmation && state.listSlotInCard.isNotEmpty()) {
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
                    PaymentConfirmComposable(
                        initSetup = state.initSetup!!,
                        countDownPaymentByCash = state.countDownPaymentByCash,
                        totalAmount = state.totalAmount,
                        onClickChooseAnotherMethodPayment = { viewModel.chooseAnotherMethodPayment() },
                        onClickBackInPayment = { viewModel.backInPayment() }
                    )
                }
            }
            if(state.isShowBigAds) {
                BigAdsComposable(
                    context = context,
                    listAds = state.listAds,
                    onClickHideAds = {
                        updateInteractionTime()
                        viewModel.hideBigAds()
                    }
                )
            }
        }
    }
}