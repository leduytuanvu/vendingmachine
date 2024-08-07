package com.combros.vendingmachine.features.settings.presentation.setupSlot.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.core.util.toChooseNumber
import com.combros.vendingmachine.core.util.toChooseNumberMoney
import com.combros.vendingmachine.features.settings.domain.model.Slot

@Composable
fun ChooseNumberComposable(
    isChooseNumber: Boolean,
    isChooseMoney: Boolean,
    isInventory: Boolean,
    slot: Slot?,
    hideDialogChooseNumber: () -> Unit,
    chooseNumber: (number: Int) -> Unit,
) {
    if(isChooseNumber) {
        Dialog(
            onDismissRequest = { hideDialogChooseNumber() },
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            items(50) { index ->
                                CustomButtonComposable(
                                    title = if (isChooseMoney) {
                                        (index).toChooseNumberMoney()
                                    } else {
                                        (index).toChooseNumber()
                                    },
                                    titleAlignment = TextAlign.Center,
                                    fontSize = 18.sp,
                                    height = 80.dp,
                                    cornerRadius = 4.dp,
                                    backgroundColor = if(isInventory) {
                                        if(index > slot!!.capacity) {
                                            Color.Gray
                                        } else {
//                                            Color(0XFFF37024)
                                            Color(0xFFE72B28)
                                        }
                                    } else {
//                                        Color(0XFFF37024)
                                        Color(0xFFE72B28)
                                    }
                                ) {
                                    if(isInventory) {
                                        if(index <= slot!!.capacity) {
                                            chooseNumber(index)
                                        }
                                    } else {
                                        chooseNumber(index)
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}