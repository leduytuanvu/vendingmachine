package com.leduytuanvu.vendingmachine.common.base.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditTextComposable(initText: String = "", keyboardTypeNumber: Boolean = false, onTextChanged: (String) -> Unit) {
    var text by remember { mutableStateOf(TextFieldValue(initText)) }

    TextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onTextChanged(newText.text)
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontSize = 20.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if(keyboardTypeNumber) KeyboardType.Number else KeyboardType.Text
        )
    )
}