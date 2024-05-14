package com.leduytuanvu.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleTextComposable(
    title: String,
    fontSize: TextUnit = 26.sp,
    paddingTop: Dp = 50.dp,
    paddingBottom: Dp = 50.dp,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        modifier = Modifier
            .padding(bottom = paddingBottom, top = paddingTop)
            .fillMaxWidth(),
        text = title,
        textAlign = textAlign,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
    )
}