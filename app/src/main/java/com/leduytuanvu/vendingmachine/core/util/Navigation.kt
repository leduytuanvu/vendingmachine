package com.leduytuanvu.vendingmachine.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.features.settings.presentation.screens.SettingsScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.screens.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leduytuanvu.vendingmachine.features.home.presentation.screens.HomeScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.screens.SetupPortScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.screens.SetupProductScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.screens.SetupSlotScreen
import com.leduytuanvu.vendingmachine.features.settings.presentation.screens.ViewLogScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.screens.InitSettingScreen

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
            InitSettingScreen(navController)
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
    }
}