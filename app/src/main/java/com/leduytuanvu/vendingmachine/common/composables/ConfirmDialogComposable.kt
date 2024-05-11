package com.leduytuanvu.vendingmachine.common.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
fun ConfirmDialogComposable(isConfirm: Boolean, state: SettingsViewState, viewModel: SettingsViewModel, navController: NavHostController) {
    val context = LocalContext.current
    if (isConfirm) {
        Dialog(
            onDismissRequest = { viewModel.hideDialogConfirm() },
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            Box(
                modifier = Modifier
                    .width(500.dp)
                    .height(260.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    content = {
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .clickable {
                                        viewModel.hideDialogConfirm()
                                    },
                                alignment = Alignment.TopEnd,
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = ""
                            )
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        BodyTextComposable(
                            title = state.titleConfirm,
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Row {
                            CustomButtonComposable(
                                title = "OK",
                                height = 65.dp,
                                width = 160.dp,
                                fontSize = 20.sp,
                                cornerRadius = 4.dp,
                                titleAlignment = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            ) {
                                when(state.nameFunction) {
                                    "removeProduct" -> viewModel.removeProduct()
                                    "fullInventory" -> viewModel.fullInventory()
                                    "loadLayoutFromServer" -> viewModel.loadLayoutFromServer()
                                    "downloadProduct" -> viewModel.downloadProduct(context)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            CustomButtonComposable(
                                title = "CANCEL",
                                height = 65.dp,
                                width = 160.dp,
                                fontSize = 20.sp,
                                cornerRadius = 4.dp,
                                titleAlignment = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            ) {
                                viewModel.hideDialogConfirm()
                            }
                        }
                    }
                )
            }
        }
    }
}

//@Preview
//@Composable
//fun ConfirmDialogPreview() {
//    ConfirmDialogComposable(
//        true,
//
//    )
//}