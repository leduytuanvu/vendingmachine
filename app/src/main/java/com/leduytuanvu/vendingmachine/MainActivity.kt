package com.leduytuanvu.vendingmachine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.TypeTXCommunicateAvf
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
//import androidx.room.Room
//import com.leduytuanvu.vendingmachine.core.room.VendingMachineDatabase
import com.leduytuanvu.vendingmachine.core.util.Navigation
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.ui.theme.VendingmachineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var portConnectionDataSource: PortConnectionDatasource
    private val crashHandler = Thread.getDefaultUncaughtExceptionHandler()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            restartApp()
        }
        setContent {
            hideStatusBar()
            VendingmachineTheme {
                val navController = rememberNavController()
                val lifecycleOwner = LocalLifecycleOwner.current.lifecycle
                LaunchedEffect(key1 = lifecycleOwner) {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        EventBus.events.collect { event ->
                            when (event) {
                                is Event.Toast -> {
                                    Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                                }
                                is Event.NavigateToHomeScreen -> {
                                    navController.navigate(Screens.SettingScreenRoute.route)
                                }
                                is Event.NavigateToSetupSlotScreen -> {
                                    navController.navigate(Screens.SetupSlotScreenRoute.route)
                                }
                            }
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(navController)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closePort()
    }

    private fun closePort() {
        portConnectionDataSource.closeVendingMachinePort()
        portConnectionDataSource.closeCashBoxPort()
    }

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun restartApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = intent
            finish()
            startActivity(intent)
            android.os.Process.killProcess(android.os.Process.myPid())
        }, 1000)
    }
}

class ScheduledTaskWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val portConnectionDatasource = PortConnectionDatasource()
    private val byteArrays = ByteArrays()
    override fun doWork(): Result {
        val taskName = inputData.getString("TASK_NAME")

        return when (taskName) {
            "TurnOnLightTask" -> {
                Logger.debug("task scheduled turn on light")
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArrays.vmTurnOnLight,

                )
                Result.success()
            }
            "TurnOffLightTask" -> {
                Logger.debug("task scheduled turn off light")
                portConnectionDatasource.sendCommandVendingMachine(
                    byteArrays.vmTurnOffLight,

                )
                Result.success()
            }
            "ResetApp" -> {
                Logger.debug("task scheduled reset app")
                val appContext = applicationContext
                restartApp(appContext)
                Result.success()
            }
            else -> {
                Result.failure()
            }
        }
    }
}


class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var localStorageDatasource: LocalStorageDatasource
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val initSetup: InitSetup? = localStorageDatasource.getDataFromPath(pathFileInitSetup)
            if (initSetup != null) {
                if(initSetup.autoStartApplication=="ON") {
                    Log.d("BootReceiver", "Device booted, starting MainActivity...")
                    val activityIntent = Intent(context, MainActivity::class.java)
                    activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(activityIntent)
                }
            }
        }
    }
}

fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
}