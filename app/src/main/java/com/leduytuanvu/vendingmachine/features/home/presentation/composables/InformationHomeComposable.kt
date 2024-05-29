package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InformationHomeComposable(
    navController: NavHostController,
    vendCode: String,
) {
    var checkLeft by remember { mutableIntStateOf(0) }
    var checkCenter by remember { mutableIntStateOf(0) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(lastInteractionTime) {
        val currentInteractionTime = lastInteractionTime
        delay(10000)
        if (currentInteractionTime == lastInteractionTime) {
            checkLeft = 0
            checkCenter = 0
        }
    }
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
                .pointerInteropFilter {
                    when (it.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            checkLeft++
                            checkCenter = 0
                            lastInteractionTime = System.currentTimeMillis()
                            true
                        }
                        else -> false
                    }
                },
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
                Text(text = vendCode, color = Color.White, fontSize = 13.sp)
            }
        }
        Box(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .pointerInteropFilter {
                when (it.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        if(checkLeft==3 && checkCenter ==6) {
                            navController.navigate(Screens.LoginScreenRoute.route)
                        }
                        if(checkLeft == 3) {
                            checkCenter++
                        }
                        lastInteractionTime = System.currentTimeMillis()
                        true
                    }
                    else -> false
                }
            },
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