package com.combros.vendingmachine.features.home.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.combros.vendingmachine.core.util.getCurrentDateTime
import kotlinx.coroutines.delay

@Composable
fun DatetimeHomeComposable(
    getTempStatusNetworkAndPower: () -> Unit,
    temp1: String,
    temp2: String,
) {
    var currentDateTime by remember { mutableStateOf(getCurrentDateTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60 * 1000L)
            currentDateTime = getCurrentDateTime()
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(300 * 1000L)
            getTempStatusNetworkAndPower()
        }
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .background(Color(0xFFA31412)),
        Arrangement.Center,
        Alignment.CenterVertically,
    ) {
        Text(
            "Nhiệt độ: ${temp1.ifEmpty { temp2.ifEmpty { "" } }}${if((temp1.isNotEmpty() && temp1 != "không thể kết nối") || (temp2.isNotEmpty() && temp2 != "không thể kết nối")) "℃" else ""}",
            modifier = Modifier.padding(start = 6.dp),
            fontSize = 13.sp,
            color = Color.White,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            currentDateTime,
            modifier = Modifier.padding(end = 6.dp),
            fontSize = 13.sp,
            color = Color.White,
        )
    }
}