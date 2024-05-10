package com.leduytuanvu.vendingmachine.features.settings.presentation.screens

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
import com.leduytuanvu.vendingmachine.common.composables.ButtonComposable
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.composables.ItemProductComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.leduytuanvu.vendingmachine.common.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.common.composables.HandlePermissionsComposable

@Composable
internal fun SetupProductScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HandlePermissionsComposable()
    LaunchedEffect(key1 = viewModel) {
        viewModel.loadProductFromServer()
    }
    SetupProductContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SetupProductContent(
    state: SettingsViewState,
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    ConfirmDialogComposable(
        isConfirm = state.isConfirm,
        state = state,
        viewModel = viewModel,
        navController = navController
    )
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) {
        Column (
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            content = {
                Row(
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    ButtonComposable(
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
                            .clickable { viewModel.showDialogConfirm("Do you want to download products?", null, "downloadProduct") },
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