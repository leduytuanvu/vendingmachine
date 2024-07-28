package com.combros.vendingmachine.features.home.presentation.composables

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.combros.vendingmachine.core.util.getCurrentDateTime
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatetimeHomeComposable(
    getTempStatusNetworkAndPower: () -> Unit,
    temp1: String,
    temp2: String,
) {
    var currentDateTime by remember { mutableStateOf(getCurrentDateTime()) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Update the date and time every minute
    LaunchedEffect(Unit) {
        while (true) {
            delay(60 * 1000L)
            currentDateTime = getCurrentDateTime()
        }
    }

    // Fetch temperature status every 5 minutes
    LaunchedEffect(Unit) {
        while (true) {
            delay(300 * 1000L)
            getTempStatusNetworkAndPower()
        }
    }

    val temperature = when {
        temp1.isNotEmpty() && temp1 != "không thể kết nối" -> temp1
        temp2.isNotEmpty() && temp2 != "không thể kết nối" -> temp2
        else -> ""
    }
    val temperatureDisplay = if (temperature.isNotEmpty()) "$temperature ℃" else ""
    Row(
        modifier = Modifier
            .height(screenHeight*0.03f) // Dynamic height based on screen size and orientation
            .fillMaxWidth()
            .background(Color(0xFFA31412))
            .padding(horizontal = 8.dp), // Horizontal padding for better spacing
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Nhiệt độ: $temperatureDisplay",
            fontSize = 13.sp, // Responsive font size
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp) // Padding to separate from edge
        )
        Text(
            text = currentDateTime,
            fontSize = 13.sp, // Responsive font size
            color = Color.White,
            modifier = Modifier.padding(end = 8.dp) // Padding to separate from edge
        )
    }
}