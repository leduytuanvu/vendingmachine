//package com.leduytuanvu.vendingmachine.features.home.presentation.composables
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.rememberAsyncImagePainter
//import coil.request.ImageRequest
//import com.leduytuanvu.vendingmachine.R
//import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
//import com.leduytuanvu.vendingmachine.common.base.presentation.composables.BodyTextComposable
//import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
//import com.leduytuanvu.vendingmachine.core.util.Logger
//import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
//import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
//import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
//
//@Composable
//fun ListProductHomeComposable(
//    listSlotInHome: ArrayList<Slot>,
//    listSlotInCard: ArrayList<Slot>,
//    initSetup: InitSetup,
//    slotShowBottom: Slot?,
//    isShowAds: Boolean,
//    localStorageDatasource: LocalStorageDatasource,
//    onClickMinusProduct: (Slot) -> Unit,
//    onClickPlusProduct: (Slot) -> Unit,
//    onClickAddProduct: (Slot) -> Unit,
//    onClickShowAds: () -> Unit,
//    getInventoryByProductCode: () -> Unit,
//    getTotal: () -> Unit,
//) {
//    Box(modifier = Modifier.fillMaxHeight()) {
//        val chunks = listSlotInHome.chunked(3)
//        Column(
//            modifier = Modifier
//                .padding(start = 20.dp, end = 20.dp)
//                .verticalScroll(rememberScrollState())
//        ) {
//            Spacer(modifier = Modifier.height(54.dp))
//            chunks.forEach { rowItems ->
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    rowItems.forEach { slot ->
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .border(
//                                    width = 0.dp,
//                                    color = Color.White,
//                                    shape = RoundedCornerShape(22.dp)
//                                )
//                                .background(
//                                    Color.White,
//                                    shape = RoundedCornerShape(22.dp)
//                                ),
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .padding(20.dp)
//                                    .fillMaxWidth()
//                                    .fillMaxHeight(),
//                                verticalArrangement = Arrangement.Center,
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                            ) {
//                                val imageModifier = Modifier
//                                    .width(150.dp)
//                                    .height(150.dp)
//                                    .clickable { }
//                                val imagePainter = if (slot.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
//                                        pathFolderImageProduct + "/${slot.productCode}.png"
//                                    )
//                                ) {
//                                    val imageRequest = ImageRequest.Builder(LocalContext.current)
//                                        .data(pathFolderImageProduct + "/${slot.productCode}.png")
//                                        .build()
//                                    rememberAsyncImagePainter(imageRequest)
//                                } else {
//                                    painterResource(id = R.drawable.image_error)
//                                }
//                                Image(
//                                    modifier = imageModifier,
//                                    painter = imagePainter,
//                                    contentDescription = ""
//                                )
//
//                                Spacer(modifier = Modifier.height(20.dp))
//
//                                Text(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(bottom = 10.dp),
//                                    text = slot.productName,
//                                    minLines = 2,
//                                    maxLines = 2,
//                                    overflow = TextOverflow.Ellipsis,
//                                    fontSize = 17.sp,
//                                )
//
//                                BodyTextComposable(
//                                    title = slot.price.toVietNamDong(),
//                                    fontSize = 19.sp,
//                                    paddingBottom = 20.dp,
//                                    color = Color(0xFFE72B28),
//                                    fontWeight = FontWeight.Bold,
//                                )
//
//                                if (listSlotInCard.isNotEmpty() && getInventoryByProductCode(slot.productCode) != -1) {
//                                    Box(
//                                        modifier = Modifier
//                                            .height(60.dp)
//                                            .border(
//                                                width = 0.dp,
//                                                color = Color(0xFFE72B28),
//                                                shape = RoundedCornerShape(50.dp)
//                                            ),
//                                    ) {
//                                        Row(
//                                            modifier = Modifier
//                                                .fillMaxSize()
//                                                .padding(horizontal = 20.dp),
//                                            horizontalArrangement = Arrangement.SpaceBetween,
//                                            verticalAlignment = Alignment.CenterVertically,
//                                        ) {
//                                            Image(
//                                                modifier = Modifier
//                                                    .height(30.dp)
//                                                    .width(30.dp)
//                                                    .clickable {
//                                                        onClickMinusProduct(
//                                                            slot
//                                                        )
//                                                    },
//                                                alignment = Alignment.Center,
//                                                painter = painterResource(id = R.drawable.image_minus),
//                                                contentDescription = ""
//                                            )
//                                            Text(
//                                                "${viewModel.getInventoryByProductCode(slot.productCode)}",
//                                                fontSize = 19.sp,
//                                            )
//                                            Image(
//                                                modifier = Modifier
//                                                    .height(30.dp)
//                                                    .width(30.dp)
//                                                    .clickable {
//                                                        viewModel.plusProductDebounced(
//                                                            slot
//                                                        )
//                                                    },
//                                                alignment = Alignment.Center,
//                                                painter = painterResource(id = R.drawable.image_plus),
//                                                contentDescription = ""
//                                            )
//                                        }
//                                    }
//                                } else {
//                                    Button(
//                                        onClick = {
//                                            viewModel.addProductDebounced(slot)
//                                        },
//                                        modifier = Modifier
//                                            .height(60.dp)
//                                            .border(
//                                                width = 0.dp,
//                                                color = Color(0xFFE72B28),
//                                                shape = RoundedCornerShape(50.dp)
//                                            ),
//                                        colors = ButtonDefaults.buttonColors(
//                                            Color(0xFFE72B28),
//                                            contentColor = Color.Black
//                                        ),
//                                        shape = RoundedCornerShape(50.dp),
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.fillMaxWidth(),
//                                            Arrangement.Center,
//                                            Alignment.CenterVertically,
//                                        ) {
//                                            Image(
//                                                modifier = Modifier
//                                                    .padding(end = 6.dp)
//                                                    .height(30.dp),
//                                                alignment = Alignment.Center,
//                                                painter = painterResource(id = R.drawable.image_select_to_buy),
//                                                contentDescription = ""
//                                            )
//                                            Text("Chọn mua", color = Color.White, fontSize = 18.sp)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // Add empty slots to fill the row if the row is not complete
//                    repeat(3 - rowItems.size) {
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//
//            Spacer(modifier = Modifier.height(120.dp))
//        }
//        Row {
//            Box(
//                modifier = Modifier
//                    .padding(top = 14.dp)
//                    .background(
//                        Color(0xFFF59E0B),
//                        shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
//                    )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .padding(horizontal = 12.dp)
//                        .height(80.dp),
//                    Arrangement.Center,
//                    Alignment.CenterVertically
//                ) {
//                    Column(
//                        modifier = Modifier
//
//                    ) {
//                        Text("Số dư tiền mặt", fontSize = 15.sp, color = Color.White)
//                        Text(initSetup.currentCash.toVietNamDong(), fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
//                    }
//                    Spacer(modifier = Modifier.width(10.dp))
//                    Image(
//                        modifier = Modifier
//                            .height(44.dp)
//                            .width(44.dp)
//                            .clickable { },
//                        alignment = Alignment.Center,
//                        painter = painterResource(id = R.drawable.image_withdraw),
//                        contentDescription = ""
//                    )
//                }
//            }
//            Spacer(modifier = Modifier.weight(1f))
//            if(!state.isShowAds) {
//                Button(
//                    modifier = Modifier
//                        .padding(end = 14.dp, top = 14.dp)
//                        .background(
//                            color = Color(0xFF9CA3AF),
//                            shape = RoundedCornerShape(4.dp) // Set the shape here
//                        ),
//                    colors = ButtonDefaults.buttonColors(
//                        Color(0xFF9CA3AF), // Button background color
//                        contentColor = Color.Black
//                    ),
//                    shape = RoundedCornerShape(4.dp), // Set the shape again for consistency
//                    onClick = { viewModel.showAdsDebounced() },
//                ) {
//                    Text(
//                        text = "Bật quảng cáo",
//                        color = Color.White,
//                        fontSize = 16.sp,
//                    )
//                }
//            }
//        }
//        if(state.listSlotInCard.isNotEmpty()) {
//            Row (
//                modifier = Modifier
//                    .height(120.dp)
//                    .background(Color.White)
//                    .padding(horizontal = 20.dp)
//                    .align(Alignment.BottomCenter),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                val imageModifier = Modifier
//                    .width(80.dp)
//                    .height(80.dp)
//                val imagePainter = if (state.slotAtBottom!!.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
//                        pathFolderImageProduct +"/${state.slotAtBottom.productCode}.png")) {
//                    val imageRequest = ImageRequest.Builder(LocalContext.current)
//                        .data(pathFolderImageProduct +"/${state.slotAtBottom.productCode}.png")
//                        .build()
//                    rememberAsyncImagePainter(imageRequest)
//                } else {
//                    painterResource(id = R.drawable.image_add_slot)
//                }
//                Image(
//                    modifier = imageModifier,
//                    painter = imagePainter,
//                    contentDescription = ""
//                )
//                Spacer(modifier = Modifier.width(10.dp))
//                Column() {
//                    Text(if(state.slotAtBottom != null) state.slotAtBottom.productName else "")
//                    Text(if(state.slotAtBottom!=null) "(Số lượng ${state.slotAtBottom.inventory})" else "")
//                    Text(if(state.slotAtBottom!=null) state.slotAtBottom.price.toVietNamDong() else "", color = Color(0xFFE72B28), fontWeight = FontWeight.Bold)
//                }
//                Spacer(modifier = Modifier.weight(1f))
//                Button(
//                    onClick = { viewModel.showPaymentDebounced() },
//                    modifier = Modifier
//                        .height(80.dp)
//                        .wrapContentWidth()
//                        .border(
//                            width = 0.dp,
//                            color = Color(0xFFE72B28),
//                            shape = RoundedCornerShape(10.dp)
//                        ),
//                    colors = ButtonDefaults.buttonColors(
//                        Color(0xFFE72B28),
//                        contentColor = Color.Black
//                    ),
//                    shape = RoundedCornerShape(10.dp),
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 10.dp),
//                        Arrangement.Center,
//                        Alignment.CenterVertically,
//                    ) {
//                        Text("Thanh toán", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
//                        Text(" | ", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
//                        Text(getTotal().toIn, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
//                    }
//                }
//            }
//        }
//    }
//}