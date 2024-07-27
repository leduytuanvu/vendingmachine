package com.combros.vendingmachine.common.base.presentation.composables

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.combros.vendingmachine.R

@Composable
fun ConfirmDialogComposable(
    isConfirm: Boolean,
    titleDialogConfirm: String,
    titleLeft: String = "OK",
    titleRight: String = "CANCEL",
    onClickClose: () -> Unit,
    onClickConfirm: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    if (isConfirm) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .width(500.dp)
                    .height(290.dp)
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
                                        onClickClose()
                                    },
                                alignment = Alignment.TopEnd,
                                painter = painterResource(id = R.drawable.image_close),
                                contentDescription = ""
                            )
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        BodyTextComposable(
                            title = titleDialogConfirm,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            paddingLeft = 10.dp,
                            paddingRight = 10.dp,
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Row {
                            CustomButtonComposable(
                                title = titleLeft,
                                height = 65.dp,
                                width = 160.dp,
                                fontSize = 20.sp,
                                cornerRadius = 4.dp,
                                titleAlignment = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            ) {
                                onClickConfirm()
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            CustomButtonComposable(
                                title = titleRight,
                                height = 65.dp,
                                width = 160.dp,
                                fontSize = 20.sp,
                                cornerRadius = 4.dp,
                                titleAlignment = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            ) {
                                onClickClose()
                            }
                        }
                    }
                )
            }
        }
    }
}