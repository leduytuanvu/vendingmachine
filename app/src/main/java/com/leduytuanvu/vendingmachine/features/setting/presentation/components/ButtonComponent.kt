package com.leduytuanvu.vendingmachine.features.setting.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ButtonComponent(
    title: String,
    backgroundColor: Color = Color.LightGray,
    titleAlignment: TextAlign = TextAlign.Start,
    cornerRadius: Dp = 0.dp,
    height: Dp = 48.dp,
    function: () -> Unit
) {
    Button(
        onClick = function,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(height),
        colors = ButtonDefaults.buttonColors(
            backgroundColor,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = titleAlignment
        )
    }
}

@Preview
@Composable
fun ButtonComponentPreview() {
    ButtonComponent(title = "HOME") {}
}