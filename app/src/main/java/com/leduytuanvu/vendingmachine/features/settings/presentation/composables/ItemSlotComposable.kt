package com.leduytuanvu.vendingmachine.features.settings.presentation.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel

@Composable
fun ItemSlotComposable(
    slot: Slot,
    function: (isChooseMoney: Boolean) -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val localStorageDatasource = LocalStorageDatasource()
    var isChecked by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .height(560.dp)
            .padding(bottom = 10.dp)
            .border(width = 0.4.dp, color = Color.Black, shape = RoundedCornerShape(10.dp)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Text(text = "${slot.slot}", fontSize = 24.sp)
                Spacer(modifier = Modifier.weight(1f))
                val imageModifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
                    .clickable {
                        viewModel.showDialogChooseImage(slot = slot)
                    }
                val imagePainter = if (slot.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                        pathFolderImage+"/${slot.productCode}.png")) {
                    val imageRequest = ImageRequest.Builder(LocalContext.current)
                        .data(pathFolderImage+"/${slot.productCode}.png")
                        .build()
                    rememberAsyncImagePainter(imageRequest)
                } else {
                    painterResource(id = R.drawable.add_slot)
                }
                Image(
                    modifier = imageModifier,
                    painter = imagePainter,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.weight(1f))
                if(slot.productCode.isNotEmpty()) {
                    Image(
                        modifier = Modifier
                            .width(34.dp)
                            .height(34.dp)
                            .clickable {
                                viewModel.showDialogConfirm(
                                    mess = "Are you sure to delete this product?",
                                    slot,
                                    "removeProduct"
                                )
                                isChecked = false
                            },
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = ""
                    )
                } else {
                    Image(
                        modifier = Modifier
                            .width(34.dp)
                            .height(34.dp)
                            .clickable {
                                if(isChecked) {
                                    viewModel.removeSlotToStateListAddMore(slot)
                                } else {
                                    viewModel.addSlotToStateListAddMore(slot)
                                }
                                isChecked = !isChecked
                            },
                        painter = painterResource(id = if (isChecked) R.drawable.check_box else R.drawable.un_check_box),
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                slot.productName.ifEmpty { "Not have product" },
                modifier = Modifier.height(50.dp),
                maxLines = 2,
                fontSize = 18.sp,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Column {
                    Text("Inventory", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${slot.inventory}/${slot.capacity}", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                CustomButtonComposable(
                    title = "Edit",
                    wrap = true,
                    height = 60.dp,
                    cornerRadius = 4.dp
                ) {
                    viewModel.showDialogChooseNumber(slot = slot, isInventory = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Column {
                    Text("Price", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = slot.price.toVietNamDong(), fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                CustomButtonComposable(
                    title = "Edit",
                    wrap = true,
                    height = 60.dp,
                    cornerRadius = 4.dp
                ) {
                    viewModel.showDialogChooseNumber(isChooseMoney = true, slot = slot)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Column {
                    Text("Capacity", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${slot.capacity}", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                CustomButtonComposable(
                    title = "Edit",
                    wrap = true,
                    height = 60.dp,
                    cornerRadius = 4.dp
                ) {
                    viewModel.showDialogChooseNumber(slot = slot, isCapacity = true)
                }
            }

//            Spacer(modifier = Modifier.height(20.dp))
//
//            ButtonComposable(
//                title = "SLOT PAIRING",
//                height = 65.dp,
//                titleAlignment = TextAlign.Center,
//                cornerRadius = 4.dp
//            ) {
//
//            }
        }
    }
}