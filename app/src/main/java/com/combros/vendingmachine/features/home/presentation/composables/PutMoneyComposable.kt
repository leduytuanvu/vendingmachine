package com.combros.vendingmachine.features.home.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.combros.vendingmachine.R
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.core.util.toVietNamDong

@Composable
fun PutMoneyComposable(
    initSetup: InitSetup,
    countDownPaymentByCash: Long,
    totalAmount: Int,
    onClickChooseAnotherMethodPayment: () -> Unit,
    onClickBackInPayment: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    CustomButtonComposable(
        title = "Quay lại",
        wrap = true,
        height = screenHeight*0.052f,
        fontSize = 20.sp,
        cornerRadius = 4.dp,
        fontWeight = FontWeight.Bold,
        paddingTop = 22.dp,
        paddingStart = 22.dp
    ) {
        onClickBackInPayment()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight*0.8f)
            .padding(screenHeight*0.036f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .height(screenHeight*0.33f)
                .padding(bottom = screenHeight*0.034f),
            alignment = Alignment.Center,
            painter = painterResource(id = R.drawable.image_put_money),
            contentDescription = ""
        )
        Text(
            "Số tiền cần thanh toán: ${totalAmount.toVietNamDong()}",
            modifier = Modifier.padding(bottom = screenHeight*0.02f),
            fontSize = 22.sp,
        )
        Text(
            "Vuốt phẳng và nhét tiền vào khe bên dưới",
            modifier = Modifier.padding(bottom = screenHeight*0.02f),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        )
        Text(
            "Số dư: ${initSetup!!.currentCash.toVietNamDong()}",
            modifier = Modifier.padding(bottom = screenHeight*0.02f),
            fontSize = 22.sp,
        )
        Text(
            "Thời gian thanh toán còn ${countDownPaymentByCash}",
            modifier = Modifier.padding(bottom = screenHeight*0.024f),
            fontSize = 22.sp,
        )

        CustomButtonComposable(
            title = "Chọn hình thức thanh toán khác",
            wrap = true,
            fontSize = 22.sp,
            cornerRadius = 6.dp,
            height = screenHeight*0.04f,
        ) {
            onClickChooseAnotherMethodPayment()
        }
    }
}