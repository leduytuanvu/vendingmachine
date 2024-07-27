package com.combros.vendingmachine.features.home.presentation.composables

import android.content.Context
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun AdsHomeComposable(
    context: Context,
    listAds: ArrayList<String>,
    onClickHideAds: () -> Unit,
    onStartAdsHome: (path: String) -> Unit,
) {
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight*0.36f)
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                VideoView(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnCompletionListener {
                        currentVideoIndex = (currentVideoIndex + 1) % listAds.size
                        setVideoPath(listAds[currentVideoIndex])
                        val nameAds: String = listAds[currentVideoIndex].split("/").last()
                        start()
                        onStartAdsHome(nameAds)
                    }
                    if (listAds.isNotEmpty()) {
                        setVideoPath(listAds[currentVideoIndex])
                        val nameAds: String = listAds[currentVideoIndex].split("/").last()
                        start()
                        onStartAdsHome(nameAds)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
        )
        Button(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = screenHeight*0.01f, end = screenHeight*0.01f)
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
            onClick = { onClickHideAds() },
        ) {
            Text(
                text = "Tắt quảng cáo",
                color = Color.White,
                fontSize = 16.sp,
            )
        }
    }
}

