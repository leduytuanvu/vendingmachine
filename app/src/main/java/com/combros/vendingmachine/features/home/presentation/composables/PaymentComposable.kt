//package com.leduytuanvu.vendingmachine.features.home.presentation.composables
//
//import android.content.Context
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.LocalTextStyle
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.rememberAsyncImagePainter
//import coil.request.ImageRequest
//import com.leduytuanvu.vendingmachine.R
//import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
//import com.leduytuanvu.vendingmachine.core.util.pathFolderImagePayment
//import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
//import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
//import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
//
//@Composable
//fun PaymentComposable(
//    context: Context,
//    listAds: ArrayList<String>,
//    listSlotInCard: ArrayList<Slot>,
//    onClickBack: () -> Unit,
//    onClickMinus: (Slot) -> Unit,
//) {
//    Column(modifier = Modifier.padding(20.dp)) {
//        CustomButtonComposable(
//            title = "Quay lại",
//            wrap = true,
//            paddingBottom = 36.dp,
//            fontSize = 20.sp,
//            height = 60.dp,
//            fontWeight = FontWeight.Bold,
//            cornerRadius = 6.dp,
//        ) {
//            onClickBack()
//        }
//        Column(
//            modifier = Modifier
//                .heightIn(max = 400.dp)
//                .fillMaxWidth()
//                .verticalScroll(rememberScrollState())
//        ) {
//            listSlotInCard.forEach { item ->
//                Row (
//                    modifier = Modifier.padding(bottom = 10.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    val imageModifier = Modifier
//                        .width(130.dp)
//                        .height(130.dp)
//                        .padding(end = 10.dp)
//                        .clickable { }
//                    val imagePainter = if (item.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
//                            pathFolderImageProduct + "/${item.productCode}.png"
//                        )
//                    ) {
//                        val imageRequest = ImageRequest.Builder(LocalContext.current)
//                            .data(pathFolderImageProduct + "/${item.productCode}.png")
//                            .build()
//                        rememberAsyncImagePainter(imageRequest)
//                    } else {
//                        painterResource(id = R.drawable.image_error)
//                    }
//                    Image(
//                        modifier = imageModifier,
//                        painter = imagePainter,
//                        contentDescription = ""
//                    )
//
//                    Column() {
//                        Text(
//                            item.productName,
//                            fontSize = 19.sp,
//                            fontWeight = FontWeight.Bold,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text("Đơn giá: ${item.price.toVietNamDong()}/lon", fontSize = 14.sp)
//                        Spacer(modifier = Modifier.height(10.dp))
//                        Row(
//                            modifier = Modifier,
//                            verticalAlignment = Alignment.CenterVertically,
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .width(50.dp)
//                                    .height(40.dp)
//                                    .border(
//                                        width = 0.2.dp,
//                                        color = Color.Gray,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .background(
//                                        Color.White,
//                                        shape = RoundedCornerShape(6.dp)
//                                    ),
//                                contentAlignment = Alignment.Center,                                            ) {
//                                Image(
//                                    modifier = Modifier
//                                        .height(20.dp)
//                                        .width(20.dp)
//                                        .clickable {
//                                            onClickMinus(item)
//                                        },
//                                    alignment = Alignment.Center,
//                                    painter = painterResource(id = R.drawable.image_minus),
//                                    contentDescription = ""
//                                )
//                            }
//
//                            Text(
//                                "${item.inventory}",
//                                fontSize = 19.sp,
//                                modifier = Modifier.padding(horizontal = 20.dp)
//                            )
//                            Box(
//                                modifier = Modifier
//                                    .width(50.dp)
//                                    .height(40.dp)
//                                    .border(
//                                        width = 0.2.dp,
//                                        color = Color.Gray,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .background(
//                                        Color.White,
//                                        shape = RoundedCornerShape(6.dp)
//                                    ),
//                                contentAlignment = Alignment.Center,
//                            ) {
//                                Image(
//                                    modifier = Modifier
//                                        .height(20.dp)
//                                        .width(20.dp)
//                                        .clickable {
//                                            viewModel.plusProductDebounced(item)
//                                        },
//                                    alignment = Alignment.Center,
//                                    painter = painterResource(id = R.drawable.image_plus),
//                                    contentDescription = ""
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.weight(1f))
//                            Text((item.price*item.inventory).toVietNamDong(), color = Color(0xFFE72B28), fontWeight = FontWeight.Bold)
//                        }
//                    }
//                }
//            }
//
//
//        }
//        Row(
//            modifier = Modifier
//                .padding(top = 28.dp)
//                .fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//        ) {
//            var text by remember { mutableStateOf("") }
//            OutlinedTextField(
//                value = text,
//                onValueChange = { text = it },
//                modifier = Modifier
//                    .padding(end = 10.dp)
//                    .height(60.dp)
//                    .weight(1f),
//                placeholder = { Text(text = "Mã giảm giá", fontSize = 18.sp) }, // Hint
//                shape = RoundedCornerShape(4.dp),
//                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp),
//                colors = TextFieldDefaults.outlinedTextFieldColors(
//                    focusedBorderColor = Color.Gray,
//                    unfocusedBorderColor = Color.Gray,
//                )
//            )
//            CustomButtonComposable(
//                title = "Áp dụng",
//                cornerRadius = 6.dp,
//                fontSize = 20.sp,
//                wrap = true,
//                height = 60.dp,
//                fontWeight = FontWeight.Bold,
//            ) {
//                viewModel.applyPromotion(text)
//            }
//        }
//        Spacer(modifier = Modifier.height(34.dp))
//        Row {
//            Text(
//                "Khuyến mãi",
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.weight(1f),
//                fontSize = 20.sp,
//            )
//            Text(
//                if(state.promotion == null) 0.toVietNamDong()
//                else state.promotion.totalDiscount!!.toVietNamDong()
//                , fontSize = 20.sp
//            )
//        }
//        Spacer(modifier = Modifier.height(28.dp))
//        Row {
//            Text(
//                "Tổng tiền thanh toán",
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.weight(1f),
//                fontSize = 20.sp,
//            )
//            Text(
//                if(state.promotion == null) state.totalAmount.toVietNamDong()
//                else state.promotion.paymentAmount!!.toVietNamDong()
//                , fontSize = 20.sp
//            )
//        }
//        Spacer(modifier = Modifier.height(28.dp))
//        Row {
//            Text(
//                "Số dư tiền mặt trên máy",
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.weight(1f),
//                fontSize = 20.sp,
//            )
//            Text("0vnd", fontSize = 20.sp)
//        }
//        Spacer(modifier = Modifier.height(28.dp))
//        Text(
//            "Hình thức thanh toán",
//            fontWeight = FontWeight.Bold,
//            fontSize = 20.sp,
//        )
//        Box(modifier = Modifier) {
//            val chunks = state.listPaymentMethod.chunked(3)
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 28.dp)
//            ) {
//                chunks.forEach { rowItems ->
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(14.dp)
//                    ) {
//                        rowItems.forEach { item ->
////                                            var isChoose by remember { mutableStateOf(false) }
////                                            if(item.methodName == "cash") {
////                                                isChoose = true
////                                            }
////                                            var nameMethodPayment by remember { mutableStateOf("cash") }
//                            Box(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(106.dp)
//                                    .padding(bottom = 14.dp)
//                                    .border(
//                                        width = if (state.nameMethodPayment == item.methodName) 2.dp else 0.4.dp,
//                                        color = if (state.nameMethodPayment == item.methodName) Color.Green else Color.Gray,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .background(
//                                        Color.White,
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .clickable {
//                                        viewModel.updateNameMethod(item.methodName!!)
//                                    },
//                                contentAlignment = Alignment.Center,
//                            ) {
//                                Row(
//                                    modifier = Modifier
//                                        .padding(start = 24.dp, end = 10.dp)
//                                        .fillMaxWidth(),
//                                    verticalAlignment = Alignment.CenterVertically,
//                                ) {
//                                    val imageModifier = Modifier
//                                        .width(44.dp)
//                                        .height(44.dp)
//                                        .clickable { }
//                                    val imagePainter = if (item.methodName!!.isNotEmpty() && localStorageDatasource.checkFileExists(
//                                            pathFolderImagePayment + "/${item.methodName}.png"
//                                        )
//                                    ) {
//                                        val imageRequest = ImageRequest.Builder(LocalContext.current)
//                                            .data(pathFolderImagePayment + "/${item.methodName}.png")
//                                            .build()
//                                        rememberAsyncImagePainter(imageRequest)
//                                    } else {
//                                        painterResource(id = R.drawable.image_error)
//                                    }
//                                    Image(
//                                        modifier = imageModifier,
//                                        painter = imagePainter,
//                                        contentDescription = ""
//                                    )
//                                    Text(
//                                        item.brief ?: "",
//                                        modifier = Modifier.padding(20.dp),
//                                        fontSize = 16.sp,
//                                        maxLines = 2,
//                                        overflow = TextOverflow.Ellipsis,
//                                    )
//                                }
//                            }
//                            // Add spacers to fill up the row if there are less than 3 items
//                            if (rowItems.size < 3) {
//                                repeat(3 - rowItems.size) {
//                                    Spacer(modifier = Modifier.weight(1f))
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        CustomButtonComposable(
//            title = "Xác nhận thanh toán",
//            cornerRadius = 6.dp,
//            titleAlignment = TextAlign.Center,
//            fontWeight = FontWeight.Bold,
//            height = 70.dp,
//            fontSize = 20.sp,
//            paddingTop = 12.dp,
//        ) {
//            viewModel.paymentConfirmation()
//        }
//    }
//}