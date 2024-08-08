package com.combros.vendingmachine.features.settings.presentation.viewLog.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.toJson
import com.combros.vendingmachine.features.settings.presentation.settings.viewModel.SettingsViewModel
import com.combros.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import kotlinx.coroutines.delay

@Composable
internal fun ViewLogScreen(
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
            if (System.currentTimeMillis() - lastInteractionTime > 600000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.ViewLogScreenRoute.route) {
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
        ViewLogContent(
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
fun ViewLogContent(
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
//                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )
                }.nestedScroll(nestedScrollConnection)) {
                    items(state.listLogServerLocal.size) {
                        index -> Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("dfghfghhhh")
                            Text(
                                "${state.listLogServerLocal[index].eventTime} ${state.listLogServerLocal[index].eventType.uppercase()}",
                                color = when (state.listLogServerLocal[index].eventType) {
                                    "fill" -> {
                                        Color(0xFF00CC66)
                                    }
                                    "error" -> {
                                        Color(0xFFFF0000)
                                    }
                                    "spring" -> {
                                        Color(0xFFFF9933)
                                    }
                                    "setup" -> {
                                        Color(0xFFFF3399)
                                    }
                                    "door" -> {
                                        Color(0xFF0033FF)
                                    }
                                    "status" -> {
                                        Color(0xFF660033)
                                    }
                                    "authy" -> {
                                        Color(0xFF00FFCC)
                                    }
                                    "temperature" -> {
                                        Color(0xFFFFCC99)
                                    }
                                    "sensor" -> {
                                        Color(0xFF666600)
                                    }
                                    else -> {
                                        Color.Blue
                                    }
                                }
                            )
                            Text(
                                "Content: ${state.listLogServerLocal[index].eventData.toJson()}:",
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "Is sent: ${state.listLogServerLocal[index].isSent}:",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        )
    }
}