package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
import com.leduytuanvu.vendingmachine.core.util.toVietNamDong
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.composables.ChooseImageComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewModel.SetupSlotViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewState.SetupSlotViewState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


@Composable
internal fun SetupSlotScreen(
    navController: NavHostController,
    viewModel: SetupSlotViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadInitSetupListSlotListProduct()
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.closePort()
        }
    }
    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.SetupSlotScreenRoute.route) {
                        inclusive = true
                    }
                    popUpTo(Screens.SettingScreenRoute.route) {
                        inclusive = true
                    }
                }
                return@LaunchedEffect
            }
            delay(1000)
        }
    }
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                lastInteractionTime = System.currentTimeMillis()
                return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                lastInteractionTime = System.currentTimeMillis()
                return super.onPostScroll(consumed, available, source)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        lastInteractionTime = System.currentTimeMillis()
                    }
                )
            }
        ) {
            SetupSlotContent(
                state = state,
                viewModel = viewModel,
                navController = navController,
                onClick = { lastInteractionTime = System.currentTimeMillis() },
                nestedScrollConnection = nestedScrollConnection,
            )
        }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupSlotContent(
    state: SetupSlotViewState,
    viewModel: SetupSlotViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    val localStorageDatasource = LocalStorageDatasource()
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = {
            onClick()
            viewModel.hideDialogWarning()
        },
    )
    ChooseNumberComposable(
        isChooseNumber = state.isChooseNumber,
        isChooseMoney = state.isChooseMoney,
        isInventory = state.isInventory,
        slot = state.slot,
        hideDialogChooseNumber = {
            onClick()
            viewModel.hideDialogChooseNumber()
        },
        chooseNumber = { number: Int ->
            onClick()
            viewModel.chooseNumber(number)
        }
    )
    ChooseImageComposable(
        isChooseImage = state.isChooseImage,
        listProduct = state.listProduct,
        listSlotAddMore = state.listSlotAddMore,
        slot = state.slot,
        onClickAddOneProduct = { product: Product ->
            onClick()
            viewModel.addSlotToLocalListSlot(product)
        },
        onClickAddMoreProduct = { product: Product ->
            onClick()
            viewModel.addMoreProductToLocalListSlot(product)
        },
        onClickClose = {
            onClick()
            viewModel.hideDialogChooseImage()
        }
    )
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = {
            onClick()
            viewModel.hideDialogConfirm()
        },
        onClickConfirm = {
            onClick()
            viewModel.selectFunction()
        },
    )
    Scaffold(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                }
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp).pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            },
            content = {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(bottom = 10.dp).pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    onClick()
                                }
                            )
                        },
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
                        onClick()
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to reset all slot in vending machine?",
                            nameFunction = "resetAllSlot",
                        )
                    })
                    ButtonSetupSlotComposable("ADD MORE", function = {
                        if (state.listSlotAddMore.size > 0) {
                            onClick()
                            viewModel.showDialogChooseImage(slot = null)
                        } else {
                            onClick()
                            viewModel.showToast("Please choose slot to add more!")
                        }
                    })
                    ButtonSetupSlotComposable("FULL INVENTORY", function = {
                        onClick()
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to set full inventory for all slot?",
                            nameFunction = "setFullInventory",
                        )
                    })
                    ButtonSetupSlotComposable("GET LAYOUT", function = {
                        onClick()
                        viewModel.showDialogConfirm(
                            mess = "Are you sure to get layout from server?",
                            nameFunction = "loadLayoutFromServer",
                        )
                    })
                }
                LazyVerticalGrid(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }.nestedScroll(nestedScrollConnection),
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),

                ) {
                    items(state.listSlot.size) { index ->
                        val slot = state.listSlot[index]
                        var isChecked by remember { mutableStateOf(false) }
                        var isLock by remember { mutableStateOf(slot.isLock) }
                        if(slot.status==1) {
                            Box(
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            onClick()
                                        }
                                    )
                                }
                                    .height(546.dp)
                                    .padding(bottom = 10.dp)
                                    .border(width = 0.4.dp, color = Color.Black, shape = RoundedCornerShape(10.dp))
                            ) {
                                Column(modifier = Modifier.padding(12.dp).pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            onClick()
                                        }
                                    )
                                }) {
                                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                onClick()
                                            }
                                        )
                                    }, horizontalArrangement = Arrangement.Start) {
                                        Text(
                                            text = if(slot.isCombine == "yes") "${slot.slot}+${slot.slot+1}" else "${slot.slot}",
                                            fontSize = 24.sp,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        val imageModifier = Modifier
                                            .width(150.dp)
                                            .height(150.dp)
                                            .clickable { viewModel.showDialogChooseImage(slot) }
                                        val imagePainter = if (slot.productCode.isNotEmpty() && localStorageDatasource.checkFileExists(
                                                pathFolderImageProduct +"/${slot.productCode}.png")) {
                                            val imageRequest = ImageRequest.Builder(LocalContext.current)
                                                .data(pathFolderImageProduct +"/${slot.productCode}.png")
                                                .build()
                                            rememberAsyncImagePainter(imageRequest)
                                        } else {
                                            painterResource(id = R.drawable.image_add_slot)
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
                                                        onClick()
                                                        viewModel.showDialogConfirm(
                                                            mess = "Are you sure to delete this product?",
                                                            slot = slot,
                                                            nameFunction = "removeSlot"
                                                        )
                                                        isChecked = false
                                                    },
                                                painter = painterResource(id = R.drawable.image_close),
                                                contentDescription = ""
                                            )
                                        } else {
                                            Image(
                                                modifier = Modifier
                                                    .width(34.dp)
                                                    .height(34.dp)
                                                    .clickable {
                                                        onClick()
                                                        if(isChecked) {
                                                            viewModel.removeSlotToStateListAddMore(slot)
                                                        } else {
                                                            viewModel.addSlotToStateListAddMore(slot)
                                                        }
                                                        isChecked = !isChecked
                                                    },
                                                painter = painterResource(id = if (isChecked) R.drawable.image_check_box else R.drawable.image_un_check_box),
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
                                            onClick()
                                            if(slot.productCode.isNotEmpty()) {
                                                viewModel.showDialogChooseNumber(slot = slot, isInventory = true)
                                            }
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
                                            onClick()
                                            if(slot.productCode.isNotEmpty()) {
                                                viewModel.showDialogChooseNumber(isChooseMoney = true, slot = slot)
                                            }
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
                                            onClick()
                                            if(slot.productCode.isNotEmpty()) {
                                                viewModel.showDialogChooseNumber(slot = slot, isCapacity = true)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    if(slot.isCombine == "yes") {
                                        if(slot.isLock) {
                                            CustomButtonComposable(
                                                title = "UNLOCK SLOT",
                                                titleAlignment = TextAlign.Center,
                                                cornerRadius = 4.dp,
                                                height = 60.dp,
                                                function = {
                                                    onClick()
                                                    isLock = false
                                                    viewModel.unlockSlot(slot)
                                                },
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        } else {
                                            CustomButtonComposable(
                                                title = "SPLIT SLOT",
                                                titleAlignment = TextAlign.Center,
                                                cornerRadius = 4.dp,
                                                height = 60.dp,
                                                function = {
                                                    onClick()
                                                    viewModel.splitSlot(slot)
                                                },
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    } else {
                                        if(isLock) {
                                            CustomButtonComposable(
                                                title = "UNLOCK SLOT",
                                                titleAlignment = TextAlign.Center,
                                                cornerRadius = 4.dp,
                                                height = 60.dp,
                                                function = {
                                                    onClick()
                                                    isLock = false
                                                    viewModel.unlockSlot(slot)
                                                },
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        } else {
                                            if (slot.slot == 10 || slot.slot == 20 || slot.slot == 30 || slot.slot == 40 || slot.slot == 50 || slot.slot == 60) {

                                            } else {
                                                val slotNext = state.listSlot[index+1]
                                                if (slot.productCode.isEmpty() && slotNext.productCode.isEmpty()) {
                                                    CustomButtonComposable(
                                                        title = "MERGE SLOT",
                                                        titleAlignment = TextAlign.Center,
                                                        cornerRadius = 4.dp,
                                                        height = 60.dp,
                                                        function = {
                                                            onClick()
                                                            viewModel.mergeSlot(slot)
                                                        },
                                                        fontSize = 20.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            Box(
                                modifier = Modifier
                                    .height(546.dp)
                                    .padding(bottom = 10.dp)
                                    .border(width = 0.4.dp, color = Color.Black, shape = RoundedCornerShape(10.dp)),
                            ) {

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