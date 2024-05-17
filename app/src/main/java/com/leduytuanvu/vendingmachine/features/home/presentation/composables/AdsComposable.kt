package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import android.content.Context
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState

@Composable
fun AdsHomeComposable(
    context: Context,
    listAds: ArrayList<String>,
    onClickHideAds: () -> Unit,
) {
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    val videoView = remember { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        AndroidView(
            factory = {
                VideoView(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnCompletionListener {
                        currentVideoIndex = (currentVideoIndex + 1) % listAds.size
                        setVideoPath(listAds[currentVideoIndex])
                        start()
                    }
                    videoView.value = this
                }
            },
            update = { view ->
                if (listAds.isNotEmpty()) {
                    view.setVideoPath(listAds[currentVideoIndex])
                    view.start()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
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