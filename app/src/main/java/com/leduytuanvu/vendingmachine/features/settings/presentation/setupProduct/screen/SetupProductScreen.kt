package com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.composables.ItemProductComposable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.HandlePermissionsComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.WarningDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewModel.SetupProductViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.viewState.SetupProductViewState

@Composable
internal fun SetupProductScreen(
    navController: NavHostController,
    viewModel: SetupProductViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HandlePermissionsComposable()
    SetupProductContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupProductContent(
    state: SetupProductViewState,
    viewModel: SetupProductViewModel,
    navController: NavHostController,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    WarningDialogComposable(
        isWarning = state.isWarning,
        titleDialogWarning = state.titleDialogWarning,
        onClickClose = { viewModel.hideDialogWarning(navController) },
    )
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        titleDialogConfirm = state.titleDialogConfirm,
        onClickClose = { viewModel.hideDialogConfirm() },
        onClickConfirm = { viewModel.downloadProductFromServer() }
    )
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column (
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            content = {
                Row(
                    modifier = Modifier.padding(top = 10.dp)
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
                    Image(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .clickable { viewModel.showDialogConfirm("Do you want to download all product from server to local?") },
                        painter = painterResource(id = R.drawable.download),
                        contentDescription = ""
                    )
                }
                LazyVerticalStaggeredGrid(
                    modifier = Modifier.padding(top = 10.dp),
                    columns = StaggeredGridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.listProduct.size) { index ->
                        ItemProductComposable(state.listProduct[index])
                    }
                }
            }
        )
    }
}