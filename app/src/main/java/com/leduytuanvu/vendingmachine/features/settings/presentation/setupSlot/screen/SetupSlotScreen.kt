package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.composables.ChooseNumberComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.composables.ChooseImageComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.composables.ItemSlotComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewModel.SetupSlotViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewState.SetupSlotViewState


@Composable
internal fun SetupSlotScreen(
    navController: NavHostController,
    viewModel: SetupSlotViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SetupSlotContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSlotContent(
    state: SetupSlotViewState,
    viewModel: SetupSlotViewModel,
    navController: NavHostController,
) {
    val localStorageDatasource = LocalStorageDatasource()
    var isChecked by remember { mutableStateOf(false) }
    LoadingDialogComposable(isLoading = state.isLoading)
    ChooseNumberComposable(
        isChooseNumber = state.isChooseNumber,
        isChooseMoney = state.isChooseMoney,
        isInventory = state.isInventory,
        slot = state.slot,
        hideDialogChooseNumber = { viewModel.hideDialogChooseNumber() },
        chooseNumber = { number: Int -> viewModel.chooseNumber(number) }
    )
    ChooseImageComposable(
        isChooseImage = state.isChooseImage,
        listProduct = state.listProduct,
        listSlotAddMore = state.listSlotAddMore,
        slot = state.slot,
        onClickAddOneProduct = { product: Product ->  viewModel.addSlotToLocalListSlot(product) },
        onClickAddMoreProduct = { product: Product ->  viewModel.addMoreProductToLocalListSlot(product) },
        onClickClose = { viewModel.hideDialogChooseImage() }
    )
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = { viewModel.hideDialogConfirm() },
        onClickConfirm = { viewModel.selectFunction() },
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { }
    ) {
        Column(
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
            content = {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomButtonComposable(
                        title = "BACK",
                        wrap = true,
                        height = 65.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                    ) {
                        navController.popBackStack()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    ButtonSetupSlotComposable("RESET", function = {
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to reset all slot in vending machine?",
                            nameFunction = "resetAllSlot",
                        )
                    })
                    ButtonSetupSlotComposable("ADD MORE", function = {
                        if (state.listSlotAddMore.size > 0) {
                            viewModel.showDialogChooseImage(slot = null)
                        } else {
                            viewModel.showToast("Please choose slot to add more!")
                        }
                    })
                    ButtonSetupSlotComposable("FULL INVENTORY", function = {
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to set full inventory for all slot?",
                            nameFunction = "setFullInventory",
                        )
                    })
                    ButtonSetupSlotComposable("GET LAYOUT", function = {
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to get layout from server?",
                            nameFunction = "loadLayoutFromServer",
                        )
                    })
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.listSlot.size) { index ->
                        var slot = state.listSlot[index]
//                        ItemSlotComposable(
//                            slot = state.slot!!,
////                            showDialogChooseImage = { viewModel.showDialogChooseImage(state.slot) },
////                            removeSlotToStateListAddMore = { viewModel.removeSlotToStateListAddMore(state.slot) },
////                            addSlotToStateListAddMore = { viewModel.removeSlotToStateListAddMore(state.slot) }
//                        )
                        Box(
                            modifier = Modifier
                                .height(560.dp)
                                .padding(bottom = 10.dp)
                                .border(width = 0.4.dp, color = Color.Black, shape = RoundedCornerShape(10.dp)),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Text(text = "${state.listSlot[index].slot}", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.weight(1f))
                                    val imageModifier = Modifier
                                        .width(150.dp)
                                        .height(150.dp)
                                        .clickable {
//                        showDialogChooseImage(slot)
                                        }
                                    val imagePainter = if (state.listSlot[index].productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                            pathFolderImage +"/${state.listSlot[index].productCode}.png")) {
                                        val imageRequest = ImageRequest.Builder(LocalContext.current)
                                            .data(pathFolderImage +"/${state.listSlot[index].productCode}.png")
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
                                    if(state.listSlot[index].productCode.isNotEmpty()) {
                                        Image(
                                            modifier = Modifier
                                                .width(34.dp)
                                                .height(34.dp)
                                                .clickable {
//                                viewModel.showDialogConfirm(
//                                    mess = "Are you sure to delete this product?",
//                                    slot,
//                                    "removeProduct"
//                                )
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
//                                    removeSlotToStateListAddMore(slot)
                                                    } else {
//                                    addSlotToStateListAddMore(slot)
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
                                    state.listSlot[index].productName.ifEmpty { "Not have product" },
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
                                        Text(text = "${state.listSlot[index].inventory}/${state.listSlot[index].capacity}", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    CustomButtonComposable(
                                        title = "Edit",
                                        wrap = true,
                                        height = 60.dp,
                                        cornerRadius = 4.dp
                                    ) {
//                    viewModel.showDialogChooseNumber(slot = slot, isInventory = true)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Column {
                                        Text("Price", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = state.listSlot[index].price.toVietNamDong(), fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    CustomButtonComposable(
                                        title = "Edit",
                                        wrap = true,
                                        height = 60.dp,
                                        cornerRadius = 4.dp
                                    ) {
//                    viewModel.showDialogChooseNumber(isChooseMoney = true, slot = slot)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                    Column {
                                        Text("Capacity", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "${state.listSlot[index].capacity}", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    CustomButtonComposable(
                                        title = "Edit",
                                        wrap = true,
                                        height = 60.dp,
                                        cornerRadius = 4.dp
                                    ) {
//                    viewModel.showDialogChooseNumber(slot = slot, isCapacity = true)
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
                }
            }
        )
    }
}

@Composable
fun ButtonSetupSlotComposable(title: String, function: () -> Unit) {
    CustomButtonComposable(
        title = title,
        titleAlignment = TextAlign.Start,
        cornerRadius = 4.dp,
        height = 65.dp,
        paddingStart = 4.dp,
        wrap = true,
        function = function,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    )
}