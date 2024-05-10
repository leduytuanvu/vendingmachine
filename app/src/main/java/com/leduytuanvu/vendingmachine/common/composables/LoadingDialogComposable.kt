package com.leduytuanvu.vendingmachine.common.composables

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leduytuanvu.vendingmachine.features.splash.presentation.screens.InitSettingContent
import com.leduytuanvu.vendingmachine.features.splash.presentation.view_state.SplashViewState

@Composable
fun LoadingDialogComposable(isLoading: Boolean) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { /*TODO*/ },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
//            Box(
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    modifier = Modifier.padding(0.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    content = {
//                        CircularProgressIndicator(modifier = Modifier.padding(bottom = 30.dp), color = Color.Black, strokeWidth = 2.dp)
//                        Text(text = "Loading")
//                    }
//                )
//            }
        }
    }
}

@Preview
@Composable
fun LoadingDialogPreview() {
    LoadingDialogComposable(
        true
    )
}