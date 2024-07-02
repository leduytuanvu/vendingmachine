package com.combros.vendingmachine.features.auth.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.combros.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.combros.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleAndEditTextComposable
import com.combros.vendingmachine.common.base.presentation.composables.TitleTextComposable
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.features.auth.presentation.viewModel.AuthViewModel
import com.combros.vendingmachine.features.auth.presentation.viewState.AuthViewState
import kotlinx.coroutines.delay

@Composable
internal fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Launch a coroutine that checks for inactivity
    LaunchedEffect(lastInteractionTime) {
        while (true) {
            if (System.currentTimeMillis() - lastInteractionTime > 60000) { // 60 seconds
                navController.navigate(Screens.HomeScreenRoute.route) {
                    popUpTo(Screens.LoginScreenRoute.route) {
                        inclusive = true
                    }
                }
                return@LaunchedEffect
            }
            delay(1000)
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
        LoginContent(
            state = state,
            viewModel = viewModel,
            navController = navController,
            onClick = { lastInteractionTime = System.currentTimeMillis() },
        )
    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginContent(
    state: AuthViewState,
    viewModel: AuthViewModel,
    navController: NavHostController,
    onClick: () -> Unit,
) {
    var inputUsername by remember { mutableStateOf("admin") }
    var inputPassword by remember { mutableStateOf("AVF@1234") }

    val context: Context = LocalContext.current

    LoadingDialogComposable(isLoading = state.isLoading)
//    WarningDialogComposable(
//        isWarning = state.isWarning,
//        titleDialogWarning = state.titleDialogWarning,
//        onClickClose = { viewModel.hideDialogWarning() },
//    )
    Scaffold(modifier = Modifier.fillMaxSize().fillMaxSize().pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                onClick()
            }
        )
    }) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth().fillMaxSize().pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onClick()
                    }
                )
            },
            content = {
                CustomButtonComposable(
                    title = "BACK",
                    wrap = true,
                    height = 70.dp,
                    fontSize = 20.sp,
                    cornerRadius = 4.dp,
                    paddingBottom = 10.dp,
                    fontWeight = FontWeight.Bold,
                ) { navController.popBackStack() }

                TitleTextComposable(title = "LOGIN TO SETTINGS MACHINE")
                TitleAndEditTextComposable(title = "Enter username") {
                    onClick()
                    inputUsername = it
                }
                TitleAndEditTextComposable(title = "Enter password", keyboardTypePassword = true) {
                    onClick()
                    inputPassword = it
                }
                Spacer(modifier = Modifier.weight(1f))
                CustomButtonComposable(
                    title = "LOGIN",
                    titleAlignment = TextAlign.Center,
                    cornerRadius = 4.dp,
                    height = 70.dp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ) {
                    onClick()
                    viewModel.login(
                        context,
                        inputUsername,
                        inputPassword,
                        navController,
                    )
                }
            }
        )
    }
}