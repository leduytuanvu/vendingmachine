package com.combros.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BodyTextComposable(
    title: String,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontSize: TextUnit = 20.sp,
    paddingBottom: Dp = 0.dp,
    paddingLeft: Dp = 0.dp,
    paddingRight: Dp = 0.dp,
    textAlign: TextAlign = TextAlign.Start,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Black,
) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(bottom = paddingBottom, start = paddingLeft, end = paddingRight),
        text = title,
        textAlign = textAlign,
        lineHeight = lineHeight,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
    )
}