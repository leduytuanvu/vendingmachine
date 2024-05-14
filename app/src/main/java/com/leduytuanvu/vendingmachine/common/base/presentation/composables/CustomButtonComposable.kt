package com.leduytuanvu.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomButtonComposable(
    title: String,
    backgroundColor: Color = Color(0xFFE72B28),
    titleAlignment: TextAlign = TextAlign.Start,
    cornerRadius: Dp = 0.dp,
    wrap: Boolean = false,
    height: Dp = 48.dp,
    width: Dp = 0.dp,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp = 0.dp,
    paddingStart: Dp = 0.dp,
    paddingEnd: Dp = 0.dp,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    function: () -> Unit
) {
    Button(
        onClick = function,
        modifier = Modifier
            .padding(top = paddingTop, bottom = paddingBottom, start = paddingStart, end = paddingEnd)
            .height(height)
            .border(width = 0.dp, color = backgroundColor, shape = RoundedCornerShape(cornerRadius))
            .let {
                if (wrap) {
                    it.wrapContentWidth()
                } else if (width != 0.dp) {
                    it.width(width)
                } else {
                    it.fillMaxWidth()
                }
            },
        colors = ButtonDefaults.buttonColors(
            backgroundColor,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Text(
            text = title,
            fontSize = fontSize,
            color = Color.White,
            textAlign = titleAlignment,
            fontWeight = fontWeight,
            modifier = Modifier.let {
                if (wrap) {
                    it.wrapContentWidth()
                } else {
                    it.fillMaxWidth()
                }
            }
        )
    }
}