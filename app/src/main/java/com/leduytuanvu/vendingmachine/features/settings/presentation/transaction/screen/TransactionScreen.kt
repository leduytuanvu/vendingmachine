package com.leduytuanvu.vendingmachine.features.settings.presentation.transaction.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.toJson
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import kotlinx.coroutines.delay

@Composable
internal fun TransactionScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewModel) {
        viewModel.getAllLogServerLocal()
    }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.TransactionScreenRoute.route) {
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
        object : NestedScrollConnection {
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
        TransactionContent(
            state = state,
//        viewModel = viewModel,
            navController = navController,
            onClick = { lastInteractionTime = System.currentTimeMillis() },
            nestedScrollConnection = nestedScrollConnection,
        )
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TransactionContent(
    state: SettingsViewState,
//    viewModel: SettingsViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
//    val itemsLog = listOf(
//        AnnotatedString("Error log"),
//    )
//
//    var selectedItem by remember { mutableStateOf(AnnotatedString("Error log")) }

    LoadingDialogComposable(isLoading = state.isLoading)

    Scaffold(
        modifier = Modifier.fillMaxSize().pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                }
            )
        }.nestedScroll(nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp).pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }.nestedScroll(nestedScrollConnection),
            content = {
                CustomButtonComposable(
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

                Text("Thời gian kết phiên trước: ")
                Text("Thời gian tính phiên hiện tại: ")
                Text("Tổng giao dịch bằng tiền mặt: ")
                Text("Số lượng giao dịch tiền mặt: ")
                Text("Tổng giao dịch tiền thanh toán online: ")
                Text("Số lượng giao dịch thanh toán online: ")
                Text("Số tờ tiền trong hộp thối: ")
                Row() {
                    CustomButtonComposable(
                        title = "Kết phiên tháng",
                        wrap = true,
                        height = 65.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                        paddingEnd = 10.dp,
                    ) {
                        navController.popBackStack()
                    }
                    CustomButtonComposable(
                        title = "Kết phiên ngày",
                        wrap = true,
                        height = 65.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                    ) {
                        navController.popBackStack()
                    }
                }
            }
        )
    }
}