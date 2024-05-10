package com.leduytuanvu.vendingmachine.common.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TitleAndEditTextComposable(
    title: String,
    onTextChanged: (String) -> Unit,
) {
    Spacer(modifier = Modifier.height(10.dp))
    BodyTextComposable(title = title)
    Spacer(modifier = Modifier.height(14.dp))
    EditTextComposable() { inputText -> onTextChanged(inputText) }
    Spacer(modifier = Modifier.height(20.dp))
}