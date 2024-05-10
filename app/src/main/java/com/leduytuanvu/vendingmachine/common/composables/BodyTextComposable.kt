package com.leduytuanvu.vendingmachine.common.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BodyTextComposable(
    title: String,
    fontSize: TextUnit = 20.sp,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = title,
        textAlign = textAlign,
        fontSize = fontSize,
    )
}