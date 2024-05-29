package com.leduytuanvu.vendingmachine.features.auth.presentation.screen

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.BodyTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.CustomButtonComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.LoadingDialogComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleAndEditTextComposable
import com.leduytuanvu.vendingmachine.common.base.presentation.composables.TitleTextComposable
import com.leduytuanvu.vendingmachine.features.auth.presentation.viewModel.AuthViewModel
import com.leduytuanvu.vendingmachine.features.auth.presentation.viewState.AuthViewState

@Composable
internal fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoginContent(
        state = state,
        viewModel = viewModel,
        navController = navController,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginContent(
    state: AuthViewState,
    viewModel: AuthViewModel,
    navController: NavHostController,
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
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
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
                    inputUsername = it
                }
                TitleAndEditTextComposable(title = "Enter password", keyboardTypePassword = true) {
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