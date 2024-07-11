package com.combros.vendingmachine.features.settings.presentation.transaction.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.R
import com.combros.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.features.settings.presentation.transaction.viewModel.TransactionViewModel
import com.combros.vendingmachine.features.settings.presentation.transaction.viewState.TransactionViewState
import kotlinx.coroutines.delay

@Composable
internal fun TransactionScreen(
    navController: NavHostController,
    viewModel: TransactionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadInitTransaction()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.closePort()
        }
    }
//    LaunchedEffect(key1 = viewModel) {
//        viewModel.getAllLogServerLocal()
//    }
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
            viewModel = viewModel,
            navController = navController,
            onClick = { lastInteractionTime = System.currentTimeMillis() },
            nestedScrollConnection = nestedScrollConnection,
        )
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TransactionContent(
    state: TransactionViewState,
    viewModel: TransactionViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
) {
    LoadingDialogComposable(isLoading = state.isLoading)

    if(state.isConfirm) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier
                    .width(500.dp)
                    .height(450.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                    content = {
                        Row {
                            Spacer(modifier = Modifier.weight(1f))
                            Image(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(40.dp)
                                    .clickable {
                                        viewModel.hideDialogConfirm()
                                    },
                                alignment = Alignment.TopEnd,
                                painter = painterResource(id = R.drawable.image_close),
                                contentDescription = ""
                            )
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                        BodyTextComposable(
                            title = "Confirm the end of the session",
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            paddingLeft = 10.dp,
                            paddingRight = 10.dp,
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Column(modifier = Modifier.padding(horizontal = 20.dp)){
                            Text("ID session: ${state.sessionId}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Time start session: ${state.initSetup!!.timeStartSession}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Time close session: ${state.initSetup.timeClosingSession}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Type Session: ${state.typeConfirm}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Amount transaction by cash: ${state.amountTransactionByCash}")
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Amount transaction by online: ${state.amountTransactionByOnline}")
                            Spacer(modifier = Modifier.height(24.dp))
                            CustomButtonComposable(
                                title = "OK",
                                height = 65.dp,
                                fontSize = 20.sp,
                                cornerRadius = 4.dp,
                                wrap = false,
                                titleAlignment = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                            ) {
                                viewModel.endOfSession(state.typeConfirm)
                            }
                        }
                    }
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            }
            .nestedScroll(nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onClick()
                        }
                    )
                }
                .nestedScroll(nestedScrollConnection),
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
//                Logger.debug("build again")
                if(state.initSetup!=null) {
                    Text("Time closing session: ${state.initSetup.timeClosingSession}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Current session time: ${state.initSetup.timeStartSession}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total cash payment transactions: ${state.amountTransactionByCash}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of products sold using cash payment methods: ${state.countTransactionByCash}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total online payment transactions: ${state.amountTransactionByOnline}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of products sold using online payment methods: ${state.countTransactionByOnline}")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of bills in the rotten box: ${state.numberRottenBoxBalance}")
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    Text("Time closing monthly session:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Current monthly session time:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Time closing daily session:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Current daily session time:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total cash payment transactions:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of products sold using cash payment methods:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Total online payment transactions:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of products sold using online payment methods:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Number of bills in the rotten box:")
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Spacer(modifier = Modifier.padding(top = 12.dp))
                Row() {
                    CustomButtonComposable(
                        title = "End of monthly session",
                        wrap = true,
                        height = 65.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                        paddingEnd = 10.dp,
                    ) {
                        viewModel.showDialogConfirm("monthly")
                    }
                    CustomButtonComposable(
                        title = "End of daily session",
                        wrap = true,
                        height = 65.dp,
                        fontSize = 20.sp,
                        cornerRadius = 4.dp,
                        fontWeight = FontWeight.Bold,
                    ) {
                        viewModel.showDialogConfirm("daily")
                    }
                }
            }
        )
    }
}