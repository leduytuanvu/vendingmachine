package com.leduytuanvu.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TitleAndEditTextComposable(
    title: String,
    initText: String = "",
    paddingBottom: Dp = 20.dp,
    onTextChanged: (String) -> Unit,
) {
    if(title.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        BodyTextComposable(title = title)
        Spacer(modifier = Modifier.height(14.dp))
    }
    EditTextComposable(initText = initText) { inputText -> onTextChanged(inputText) }
    Spacer(modifier = Modifier.height(paddingBottom))
}