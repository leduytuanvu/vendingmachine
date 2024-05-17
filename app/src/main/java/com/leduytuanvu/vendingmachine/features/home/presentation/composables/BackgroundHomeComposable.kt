package com.leduytuanvu.vendingmachine.features.home.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.leduytuanvu.vendingmachine.R

@Composable
fun BackgroundHomeComposable() {
    Image(
        painter = painterResource(id = R.drawable.image_background_home),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}