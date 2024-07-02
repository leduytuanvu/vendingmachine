package com.combros.vendingmachine.features.settings.presentation.setupSlot.composables

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
import com.combros.vendingmachine.core.util.pathFolderImageProduct
import com.combros.vendingmachine.features.settings.domain.model.Product
import com.combros.vendingmachine.features.settings.domain.model.Slot
import java.io.File

@Composable
fun ChooseImageComposable(
    isChooseImage: Boolean,
    listProduct: ArrayList<Product>,
    listSlotAddMore: ArrayList<Slot>,
    slot: Slot?,
    onClickAddOneProduct: (Product) -> Unit,
    onClickAddMoreProduct: (Product) -> Unit,
    onClickClose: () -> Unit,
) {
    if(isChooseImage) {
        Dialog(
            onDismissRequest = { onClickClose() },
            properties = DialogProperties(dismissOnClickOutside = true),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(910.dp)
                    .background(Color.White),
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(10.dp)
                ) {
                    items(listProduct.size) { index ->
                        val imageFile = File(pathFolderImageProduct+"/${listProduct[index].productCode}.png")
                        val painter = rememberAsyncImagePainter(imageFile)
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clickable {
                                    if(listSlotAddMore.size>0 && slot==null) {
                                        onClickAddMoreProduct(listProduct[index])
                                    } else {
                                        onClickAddOneProduct(listProduct[index])
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}