package com.leduytuanvu.vendingmachine.core.util

sealed class Screens(val route: String) {
    data object SplashScreenRoute: Screens(route = "splash")
    data object SettingScreenRoute: Screens(route = "setting")
    data object HomeScreenRoute: Screens(route = "home")
    data object InitSettingScreenRoute: Screens(route = "init-setting")
}