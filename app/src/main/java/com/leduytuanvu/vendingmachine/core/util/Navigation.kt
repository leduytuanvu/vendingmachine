package com.leduytuanvu.vendingmachine.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.screen.SettingsScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.splash.screen.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leduytuanvu.vendingmachine.features.home.presentation.screens.HomeScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPayment.screen.SetupPaymentScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupPort.screen.SetupPortScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupProduct.screen.SetupProductScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.screen.SetupSlotScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSystem.screen.SetupSystemScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.viewLog.screen.ViewLogScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.screen.InitSetupScreen

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

        composable(Screens.InitSettingScreenRoute.route) {
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
    }
}