package com.leduytuanvu.vendingmachine.features.settings.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.composables.ButtonComposable
import com.leduytuanvu.vendingmachine.common.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleAndDropdownComposable
import com.leduytuanvu.vendingmachine.common.composables.TitleTextComposable
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_model.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState

@Composable
internal fun ViewLogScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.getLog("ERROR LOG")
    }
    ViewLogContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ViewLogContent(
    state: SettingsViewState,
    viewModel: SettingsViewModel,
    navController: NavHostController,
) {
    val itemsLog = listOf(
        AnnotatedString("ERROR LOG"),
    )

    var selectedItem by remember { mutableStateOf(AnnotatedString("ERROR LOG")) }

    LoadingDialogComposable(isLoading = state.isLoading)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {  }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = {
                ButtonComposable(
                    title = "BACK",
                    wrap = true,
                    height = 65.dp,
                    fontSize = 20.sp,
                    cornerRadius = 4.dp,
                    fontWeight = FontWeight.Bold,
                    paddingBottom = 20.dp,
                ) {
                    navController.popBackStack()
                }
                TitleAndDropdownComposable(title = "", items = itemsLog, selectedItem = selectedItem) {

                }
            }
        )
    }
}

@Composable
fun ButtonViewLogComposable(title: String, function: () -> Unit) {
    ButtonComposable(
        title = title,
        titleAlignment = TextAlign.Start,
        paddingBottom = 10.dp,
        cornerRadius = 4.dp,
        height = 65.dp,
        function = function,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    )
}