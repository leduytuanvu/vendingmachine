package com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.composables.ItemSetupProductComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.HandlePermissionsComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.screen.SetupPaymentContent
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewModel.SetupProductViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewState.SetupProductViewState
import kotlinx.coroutines.delay

@Composable
internal fun SetupProductScreen(
    navController: NavHostController,
    viewModel: SetupProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HandlePermissionsComposable()
    LaunchedEffect(Unit) {
        viewModel.loadListProduct()
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.SetupProductScreenRoute.route) {
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
        SetupProductContent(
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
fun SetupProductContent(
    state: SetupProductViewState,
    viewModel: SetupProductViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = {
            onClick()
            viewModel.hideDialogWarning()
        },
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
            viewModel.downloadListProductFromServerToLocal()
        }
    )
    Scaffold(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                }
            )
        }
    ) {
        Column (
            modifier = Modifier.padding(start = 20.dp, end = 20.dp).pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            },
            content = {
                Row(
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CustomButtonComposable(
                        title = "BACK",
                        wrap = true,
                        height = 70.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                    ) {
                        navController.popBackStack()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp)
                            .clickable {
                                onClick()
                                viewModel.showDialogConfirm("Do you want to download all product from server to local?")
                            },
                        painter = painterResource(id = R.drawable.image_download),
                        contentDescription = ""
                    )
                }
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onClick()
                            }
                        )
                    }.nestedScroll(nestedScrollConnection),
                    columns = StaggeredGridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                ) {
                    items(state.listProduct.size) { index ->
                        ItemSetupProductComposable(state.listProduct[index])
                    }
                    items(3) {
                        Spacer(modifier = Modifier.height(94.dp))
                    }
                }
            }
        )
    }
}