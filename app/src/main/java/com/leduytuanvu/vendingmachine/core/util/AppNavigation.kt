package com.leduytuanvu.vendingmachine.core.util

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.leduytuanvu.vendingmachine.features.setting.presentation.screens.SettingScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.screens.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.leduytuanvu.vendingmachine.features.home.presentation.screens.HomeScreen
import com.leduytuanvu.vendingmachine.features.splash.presentation.screens.InitSettingScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppScreen.SplashScreenRoute.route) {
        composable(AppScreen.SplashScreenRoute.route) {
            SplashScreen(navController)
        }

        composable(AppScreen.SettingScreenRoute.route) {
            SettingScreen(navController)
        }

        composable(AppScreen.HomeScreenRoute.route) {
            HomeScreen(navController)
        }

        composable(AppScreen.InitSettingScreenRoute.route) {
            InitSettingScreen(navController)
        }
    }
}