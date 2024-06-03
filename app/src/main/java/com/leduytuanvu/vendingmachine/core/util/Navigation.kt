package com.leduytuanvu.vendingmachine.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.screen.SettingsScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.splash.screen.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leduytuanvu.vendingmachine.features.auth.presentation.screen.LoginScreen
import com.leduytuanvu.vendingmachine.features.home.presentation.screens.HomeScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.screen.SetupPaymentScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.screen.SetupPortScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.screen.SetupProductScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.screen.SetupSlotScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.screen.SetupSystemScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.transaction.screen.TransactionScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.viewLog.screen.ViewLogScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.screen.InitSetupScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screens.SplashScreenRoute.route) {
        composable(Screens.SplashScreenRoute.route) {
            SplashScreen(navController)
        }

        composable(Screens.SettingScreenRoute.route) {
            SettingsScreen(navController)
        }

        composable(Screens.HomeScreenRoute.route) {
            HomeScreen(navController)
        }

        composable(Screens.InitSetupScreenRoute.route) {
            InitSetupScreen(navController)
        }

        composable(Screens.SetupSlotScreenRoute.route) {
            SetupSlotScreen(navController)
        }

        composable(Screens.SetupPortScreenRoute.route) {
            SetupPortScreen(navController)
        }

        composable(Screens.SetupProductScreenRoute.route) {
            SetupProductScreen(navController)
        }

        composable(Screens.ViewLogScreenRoute.route) {
            ViewLogScreen(navController)
        }

        composable(Screens.SetupPaymentScreenRoute.route) {
            SetupPaymentScreen(navController)
        }

        composable(Screens.SetupSystemScreenRoute.route) {
            SetupSystemScreen(navController)
        }

        composable(Screens.LoginScreenRoute.route) {
            LoginScreen(navController)
        }

        composable(Screens.TransactionScreenRoute.route) {
            TransactionScreen(navController)
        }
    }
}