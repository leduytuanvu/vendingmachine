package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.features.home.presentation.viewModel.HomeViewModel
import com.leduytuanvu.vendingmachine.features.home.presentation.viewState.HomeViewState

@Composable
fun AdsComposable(
    state: HomeViewState,
    viewModel: HomeViewModel,
    onTurnOffClick: () -> Unit,
) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    val videoAspectRatio = 16f / 9f
    val videoHeight = screenWidth / videoAspectRatio
    val videoView = remember { mutableStateOf<VideoView?>(null) }

    Box(
        modifier = Modifier
            .height(videoHeight)
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White))
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                VideoView(context).apply {
                    setBackgroundColor(Color.Transparent.toArgb())
                    setOnCompletionListener {
                        currentVideoIndex = (currentVideoIndex + 1) % state.listAds.size
                        setVideoPath(state.listAds[currentVideoIndex])
                        start()
                    }
                    videoView.value = this // Assign the current VideoView instance to the mutableStateOf
                }
            },
            update = { view ->
                // Update the view if needed when the composable function is recomposed
            }
        )

        DisposableEffect(Unit) {
            onDispose {
                // Stop video playback when the composable leaves the composition
                videoView.value?.stopPlayback()
            }
        }

        CustomButtonComposable(
            function = { onTurnOffClick() },
            title = "Turn off"
        )
    }
}