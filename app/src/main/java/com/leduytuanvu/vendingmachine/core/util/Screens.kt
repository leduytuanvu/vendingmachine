package com.leduytuanvu.vendingmachine.core.util

sealed class Screens(val route: String) {
    data object SplashScreenRoute: Screens(route = "splash")
    data object SettingScreenRoute: Screens(route = "setting")
    data object HomeScreenRoute: Screens(route = "home")
    data object InitSetupScreenRoute: Screens(route = "init_setup")
    data object SetupSlotScreenRoute: Screens(route = "setup_slot")
    data object SetupPortScreenRoute: Screens(route = "setup_port")
    data object SetupProductScreenRoute: Screens(route = "setup_product")
    data object ViewLogScreenRoute: Screens(route = "view_log")
    data object SetupSystemScreenRoute: Screens(route = "setup_system")
    data object SetupPaymentScreenRoute: Screens(route = "setup_payment")
    data object LoginScreenRoute: Screens(route = "login")
}