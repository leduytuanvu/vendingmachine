package com.leduytuanvu.vendingmachine.features.settings.presentation.screens

import android.annotation.SuppressLint
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
import com.leduytuanvu.vendingmachine.common.composables.CustomButtonComposable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leduytuanvu.vendingmachine.common.composables.ConfirmDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.composables.ChooseNumberComposable
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.composables.ChooseImageComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.composables.ItemSlotComposable
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState


@Composable
internal fun SetupSlotScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
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
    state: SettingsViewState,
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    LoadingDialogComposable(isLoading = state.isLoading)
    ChooseNumberComposable(isChooseNumber = state.isChooseNumber, state, viewModel)
    ChooseImageComposable(isChooseImage = state.isChooseImage, state, viewModel)
    ConfirmDialogComposable(isConfirm = state.isConfirm, state, viewModel, navController)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
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

                    })
                    ButtonSetupSlotComposable("ADD MORE", function = {
                        if(state.listSlotAddMore.size > 0) {
                            viewModel.showDialogChooseImage(slot = null)
                        } else {
                            viewModel.showMess("Please choose slot to add more!")
                        }
                    })
                    ButtonSetupSlotComposable("FULL INVENTORY", function = {
                        viewModel.showDialogConfirm("Are you sure to set full inventory for all?", null, "fullInventory")
                    })
                    ButtonSetupSlotComposable("GET LAYOUT", function = {
                        viewModel.showDialogConfirm("Are you sure to get layout from server?", null, "loadLayoutFromServer")
                    })
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.listSlot.size) { index ->
                        ItemSlotComposable(state.listSlot[index]){}
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