package com.leduytuanvu.vendingmachine.features.home.presentation.screens

import android.annotation.SuppressLint
import android.widget.Space
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState

@Composable
internal fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeContent(
    state: HomeViewState,
    viewModel: HomeViewModel,
    navController: NavHostController,
) {
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    val videoView = remember { mutableStateOf<VideoView?>(null) }
    val context = LocalContext.current
    val localStorageDatasource = LocalStorageDatasource()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Scaffold(modifier = Modifier.background(Color.White)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.image_background_home),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(modifier = Modifier.fillMaxSize()) {
                if(state.isShowAds) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight / 3)
                    ) {
                        AndroidView(
                            factory = {
                                VideoView(context).apply {
                                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                    setOnCompletionListener {
                                        currentVideoIndex = (currentVideoIndex + 1) % state.listAds.size
                                        setVideoPath(state.listAds[currentVideoIndex])
                                        start()
                                    }
                                    videoView.value = this
                                }
                            },
                            update = { view ->
                                if (state.listAds.isNotEmpty()) {
                                    view.setVideoPath(state.listAds[currentVideoIndex])
                                    view.start()
                                }
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .clipToBounds()
                        )
                        DisposableEffect(Unit) {
                            onDispose {
                                videoView.value?.stopPlayback()
                            }
                        }

                        Button(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 14.dp, end = 14.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                Color.Transparent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(4.dp),
                            onClick = { viewModel.hideAds() },
                        ) {
                            Text(
                                text = "Tắt quảng cáo",
                                color = Color.White,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
//                Box(
//                    modifier = Modifier
//                        .height(30.dp)
//                        .fillMaxWidth()
//                        .background(Color(0xFFA31412))
//                ) {
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .fillMaxWidth()
                            .background(Color(0xFFA31412)),
                        Arrangement.End,
                        Alignment.CenterVertically,
                    ) {
                        Text(
                            "12:30 - 27 Tháng 12, 2022",
                            modifier = Modifier.padding(end = 6.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                        )
                    }

                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth()
                        .background(Color(0xFFCB1A17)),
                    Arrangement.Center,
                    Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier
                        .weight(1f),
                    ) {
                        Column(
                            modifier = Modifier,
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
                                Text(text = "1900 99 9980", color = Color.White, fontSize = 15.sp)
                            }
                            Text(text = "AVF000043", color = Color.White, fontSize = 13.sp)
                        }
                    }
                    Box(modifier = Modifier
                        .weight(1f),
                        Alignment.Center,
                    ) {
                        Image(
                            modifier = Modifier
                                .height(26.dp)
                                .clickable {
                                    navController.navigate(Screens.SettingScreenRoute.route)
                                },
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
                                modifier = Modifier
                                    .height(20.dp)
                                    .clickable { },
                                alignment = Alignment.Center,
                                painter = painterResource(id = R.drawable.image_flags),
                                contentDescription = ""
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Tiếng việt", color = Color.White)
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxHeight()) {
                    LazyVerticalGrid(
                        modifier = Modifier.padding(start = screenWidth/26, end = screenWidth/26),
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(3) {
                            Spacer(modifier = Modifier.height(screenHeight/60))
                        }
                        items(state.listSlotShowInHome.size) {index ->
                            val slot = state.listSlotShowInHome[index]
                            var isChoose by remember { mutableStateOf(false) }
                            var numberProduct by remember { mutableIntStateOf(0) }
                            Box(
                                modifier = Modifier
                                    .height(440.dp)
                                    .border(
                                        width = 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .background(Color.White, shape = RoundedCornerShape(24.dp)),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    val imageModifier = Modifier
                                        .width(180.dp)
                                        .height(180.dp)
                                        .clickable { }
                                    val imagePainter = if (slot.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                            pathFolderImage +"/${slot.productCode}.png")) {
                                        val imageRequest = ImageRequest.Builder(LocalContext.current)
                                            .data(pathFolderImage +"/${slot.productCode}.png")
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

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp)
                                            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                                        text = slot.productName,
                                        lineHeight = 30.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 20.sp,
                                    )

                                    BodyTextComposable(
                                        title = slot.price.toVietNamDong(),
                                        paddingLeft = 20.dp,
                                        paddingRight = 20.dp,
                                        paddingBottom = 22.dp,
                                        color = Color(0xFFE72B28),
                                        fontWeight = FontWeight.Bold,
                                    )

                                    if(isChoose) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp)
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
                                                            if (numberProduct == 1) {
                                                                isChoose = false

                                                            }
                                                            numberProduct--
                                                            viewModel.minusProduct(slot)
                                                        },
                                                    alignment = Alignment.Center,
                                                    painter = painterResource(id = R.drawable.image_minus),
                                                    contentDescription = ""
                                                )
                                                Text("$numberProduct", fontSize = 19.sp)
                                                Image(
                                                    modifier = Modifier
                                                        .height(30.dp)
                                                        .width(30.dp)
                                                        .clickable {
                                                            if (numberProduct < slot.inventory) {
                                                                numberProduct++
                                                                viewModel.plusProduct(slot)
                                                            }
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
                                                isChoose = true
                                                numberProduct++
                                                viewModel.addProduct(slot)
                                            },
                                            modifier = Modifier
                                                .padding(start = 16.dp, end = 16.dp)
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
                                                        .height(30.dp)
                                                        .clickable { },
                                                    alignment = Alignment.Center,
                                                    painter = painterResource(id = R.drawable.image_select_to_buy),
                                                    contentDescription = ""
                                                )
                                                Text("Chọn mua", color = Color.White, fontSize = 19.sp)
                                            }
                                        }
                                    }


                                }
                            }
                        }
                        items(3) {
                            Spacer(modifier = Modifier.height(screenHeight/60))
                        }
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
                                    .height(screenHeight / 20),
                                Arrangement.Center,
                                Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier

                                ) {
                                    Text("Số dư tiền mặt", fontSize = 15.sp, color = Color.White)
                                    Text("50.000đ", fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Image(
                                    modifier = Modifier
                                        .height(screenHeight / 40)
                                        .width(screenHeight / 40)
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
                                onClick = { viewModel.showAds() },
                            ) {
                                Text(
                                    text = "Bật quảng cáo",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }
                    if(state.listSlotBuy.isNotEmpty()) {
                        Row (
                            modifier = Modifier
                                .height(screenHeight / 15)
                                .background(Color.White)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val imageModifier = Modifier
                                .width(100.dp)
                                .height(100.dp)
                            val imagePainter = if (state.slot!!.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                    pathFolderImage +"/${state.slot.productCode}.png")) {
                                val imageRequest = ImageRequest.Builder(LocalContext.current)
                                    .data(pathFolderImage +"/${state.slot.productCode}.png")
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
                            Column() {
                                Text(if(state.slot != null) state.slot.productName else "")
                                Text(if(state.numberProduct!=0) "(Số lượng ${state.numberProduct})" else "")
                                Text(if(state.slot!=null) state.slot.price.toVietNamDong() else "", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(
                                onClick = { viewModel.paymentNow() },
                                modifier = Modifier
                                    .padding(end = 20.dp)
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
                                    Text(20000.toVietNamDong(), color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}