package com.combros.vendingmachine.features.home.presentation.composables

import android.provider.CalendarContract.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.combros.vendingmachine.R

@Composable
fun WithdrawMoneyDialogComposable(isReturning: Boolean) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    if (isReturning) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Box(modifier = Modifier
                .height(screenHeight*0.5f)
                .width(screenHeight*0.42f)
                .background(Color.White),
            ) {
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Spacer(modifier = Modifier.height(screenHeight*0.08f))
                    Image(
                        modifier = Modifier
                            .height(screenHeight*0.28f)
                            .width(screenHeight*0.28f),
                        alignment = Alignment.Center,
                        painter = painterResource(id = R.drawable.image_put_money),
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.height(screenHeight*0.054f).width(screenHeight*0.5f))
                    Text(text = "Vui lòng nhận tiền ở hộp thối", fontSize = 20.sp)
                }
            }
        }
    }
}