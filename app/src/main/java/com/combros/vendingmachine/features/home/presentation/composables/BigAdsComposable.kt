package com.combros.vendingmachine.features.home.presentation.composables

import android.content.Context
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BigAdsComposable(
    context: Context,
    listAds: ArrayList<String>,
    onClickHideAds: () -> Unit,
    onStartBigAds: (nameAds: String) -> Unit,
) {
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .clickable { onClickHideAds() }
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                VideoView(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)

                    setOnCompletionListener {
                        currentVideoIndex = (currentVideoIndex + 1) % listAds.size
                        setVideoPath(listAds[currentVideoIndex])
                        start()
                        val nameAds: String = listAds[currentVideoIndex].split("/").last()
                        onStartBigAds(nameAds)
                    }
                    if (listAds.isNotEmpty()) {
                        setVideoPath(listAds[currentVideoIndex])
                        start()
                        val nameAds: String = listAds[currentVideoIndex].split("/").last()
                        onStartBigAds(nameAds)
                    }
                }
            },
            modifier = Modifier
                .fillMaxHeight()
                .clipToBounds()
                .background(Color.Black)
                .align(Alignment.Center)
        )
    }
}