package com.leduytuanvu.vendingmachine.features.settings.presentation.composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.leduytuanvu.vendingmachine.core.storage.LocalStorage
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState
import java.io.File

@Composable
fun ChooseImageComposable(
    isChooseImage: Boolean,
    state: SettingsViewState,
    viewModel: SettingsViewModel,
) {
    if(isChooseImage) {
        Dialog(
            onDismissRequest = { viewModel.hideDialogChooseImage() },
            properties = DialogProperties(dismissOnClickOutside = true),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(910.dp)
                    .background(Color.White),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(state.listProduct.size) { index ->
                        val imageFile = File(LocalStorage().folderImage+"/${state.listProduct[index].productCode}.png")
                        val painter = rememberAsyncImagePainter(imageFile)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp).clickable {
                                    if(state.listSlotAddMore.size>0 && state.slot==null) {
                                        Log.d("tuanvulog", "add more")
                                        viewModel.addMoreProductToListSlot(state.listProduct[index])
                                    } else {
                                        Log.d("tuanvulog", "add one")
                                        viewModel.addProductToListSlot(state.listProduct[index])
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun ChooseImagePreview() {
//    ChooseImageComposable(
//        true,
//        null,
//        SettingsViewState()
//    )
//}