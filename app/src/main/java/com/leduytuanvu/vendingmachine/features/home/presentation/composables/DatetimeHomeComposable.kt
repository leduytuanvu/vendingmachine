package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.leduytuanvu.vendingmachine.core.util.getCurrentDateTime
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatetimeHomeComposable() {
    // State variable to hold the current date and time string
    var currentDateTime by remember { mutableStateOf(getCurrentDateTime()) }

    // LaunchedEffect to update the date and time every minute
    LaunchedEffect(Unit) {
        while (true) {
            delay(60 * 1000L) // Delay for one minute
            currentDateTime = getCurrentDateTime()
        }
    }
    Row(
        modifier = Modifier
            .height(30.dp)
            .fillMaxWidth()
            .background(Color(0xFFA31412)),
        Arrangement.End,
        Alignment.CenterVertically,
    ) {
        Text(
            currentDateTime,
            modifier = Modifier.padding(end = 6.dp),
            fontSize = 13.sp,
            color = Color.White,
        )
    }
}