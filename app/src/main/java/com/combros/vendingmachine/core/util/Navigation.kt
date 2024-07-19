package com.combros.vendingmachine.core.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.combros.vendingmachine.features.settings.presentation.settings.screen.SettingsScreen
import com.combros.vendingmachine.features.splash.presentation.splash.screen.SplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.combros.vendingmachine.MainActivity
import com.combros.vendingmachine.features.auth.presentation.screen.LoginScreen
import com.combros.vendingmachine.features.home.presentation.screens.HomeScreen
import com.combros.vendingmachine.features.settings.presentation.setupPayment.screen.SetupPaymentScreen
import com.combros.vendingmachine.features.settings.presentation.setupPort.screen.SetupPortScreen
import com.combros.vendingmachine.features.settings.presentation.setupProduct.screen.SetupProductScreen
import com.combros.vendingmachine.features.settings.presentation.setupSlot.screen.SetupSlotScreen
import com.combros.vendingmachine.features.settings.presentation.setupSystem.screen.SetupSystemScreen
import com.combros.vendingmachine.features.settings.presentation.transaction.screen.TransactionScreen
import com.combros.vendingmachine.features.settings.presentation.viewLog.screen.ViewLogScreen
import com.combros.vendingmachine.features.splash.presentation.initSetup.screen.InitSetupScreen
import com.combros.vendingmachine.hideSystemUI

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation(navController: NavHostController) {
    val activity = LocalContext.current as? MainActivity
    NavHost(navController = navController, startDestination = Screens.SplashScreenRoute.route) {
        composable(Screens.SplashScreenRoute.route) {
            activity?.hideSystemUI()
            SplashScreen(navController)
        }

        composable(Screens.SettingScreenRoute.route) {
            activity?.hideSystemUI()
            SettingsScreen(navController)
        }

        composable(Screens.HomeScreenRoute.route) {
            activity?.hideSystemUI()
            HomeScreen(navController)
        }

        composable(Screens.InitSetupScreenRoute.route) {
            activity?.hideSystemUI()
            InitSetupScreen(navController)
        }

        composable(Screens.SetupSlotScreenRoute.route) {
            activity?.hideSystemUI()
            SetupSlotScreen(navController)
        }

        composable(Screens.SetupPortScreenRoute.route) {
            activity?.hideSystemUI()
            SetupPortScreen(navController)
        }

        composable(Screens.SetupProductScreenRoute.route) {
            activity?.hideSystemUI()
            SetupProductScreen(navController)
        }

        composable(Screens.ViewLogScreenRoute.route) {
            activity?.hideSystemUI()
            ViewLogScreen(navController)
        }

        composable(Screens.SetupPaymentScreenRoute.route) {
            activity?.hideSystemUI()
            SetupPaymentScreen(navController)
        }

        composable(Screens.SetupSystemScreenRoute.route) {
            activity?.hideSystemUI()
            SetupSystemScreen(navController)
        }

        composable(Screens.LoginScreenRoute.route) {
            activity?.hideSystemUI()
            LoginScreen(navController)
        }

        composable(Screens.TransactionScreenRoute.route) {
            activity?.hideSystemUI()
            TransactionScreen(navController)
        }
    }
}