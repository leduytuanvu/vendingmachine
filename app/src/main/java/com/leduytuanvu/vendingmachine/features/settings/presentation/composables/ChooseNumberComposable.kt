package com.leduytuanvu.vendingmachine.features.settings.presentation.composables

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
import com.leduytuanvu.vendingmachine.common.composables.ButtonComposable
import com.leduytuanvu.vendingmachine.core.util.toChooseNumber
import com.leduytuanvu.vendingmachine.core.util.toChooseNumberMoney
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
fun ChooseNumberComposable(
    isChooseNumber: Boolean,
    state: SettingsViewState,
    viewModel: SettingsViewModel,
) {
    if(isChooseNumber) {
        Dialog(
            onDismissRequest = { viewModel.hideDialogChooseNumber() },
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
                                ButtonComposable(
                                    title = if (state.isChooseMoney) {
                                        (index + 1).toChooseNumberMoney()
                                    } else {
                                        (index + 1).toChooseNumber()
                                    },
                                    titleAlignment = TextAlign.Center,
                                    fontSize = 18.sp,
                                    height = 80.dp,
                                    cornerRadius = 4.dp,
                                    backgroundColor = if(state.isInventory) {
                                        if(index+1 > state.slot!!.capacity) {
                                            Color.Gray
                                        } else {
                                            Color(0xFFE72B28)
                                        }
                                    } else {
                                        Color(0xFFE72B28)
                                    }
                                ) {
                                    if(state.isInventory) {
                                        if(index+1 <= state.slot!!.capacity) {
                                            viewModel.chooseNumber(index+1)
                                        }
                                    } else {
                                        viewModel.chooseNumber(index+1)
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

//@Preview
//@Composable
//fun ChooseNumberPreview() {
//    ChooseNumberComposable(true)
//}