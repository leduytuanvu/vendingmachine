package com.leduytuanvu.vendingmachine.core.util

sealed class AppScreen(val route: String) {
    data object SplashScreenRoute: AppScreen(route = "splash")
    data object SettingScreenRoute: AppScreen(route = "setting")
    data object HomeScreenRoute: AppScreen(route = "home")
    data object InitSettingScreenRoute: AppScreen(route = "init-setting")
}