package com.combros.vendingmachine.common.base.presentation.composables

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialogComposable(isLoading: Boolean) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
        }
    }
}