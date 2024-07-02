package com.leduytuanvu.vendingmachine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Navigation
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.ui.theme.VendingmachineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.leduytuanvu.vendingmachine.core.util.pathFileLogServer
import java.io.File
//import com.leduytuanvu.vendingmachine.BuildConfig

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var portConnectionDataSource: PortConnectionDatasource

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            restartApp()
        }

        // Check if the WRITE_EXTERNAL_STORAGE permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, proceed with file operations
            performFileOperations()
        } else {
            // Permission is not granted, request it
            requestPermissionLauncherReadWriteExternalStorage.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncherReadPhoneState.launch(Manifest.permission.READ_PHONE_STATE)
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
                                    Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_LONG).show()
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

    private val requestPermissionLauncherReadPhoneState = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "READ_PHONE_STATE permission is required to get SIM Serial ID", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncherReadWriteExternalStorage = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission is granted, perform file operations
            performFileOperations()
        } else {
            // Permission denied, handle accordingly (e.g., show a message)
            Toast.makeText(this, "WRITE_EXTERNAL_STORAGE permission is required to save logs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performFileOperations() {
        try {
            val file = File(pathFileLogServer)
            val parentDir = file.parentFile
            if (parentDir != null) {
                if (!parentDir.exists()) {
                    val dirCreated = parentDir.mkdirs()
                    Log.d("MainActivity", "Parent directory created: $dirCreated")
                }
                if (parentDir.exists()) {
                    if (!file.exists()) {
                        val fileCreated = file.createNewFile()
                        Log.d("MainActivity", "File created: $fileCreated")
                    }
                } else {
                    Log.d("MainActivity", "Parent directory does not exist and could not be created: ${parentDir.absolutePath}")
                }
            } else {
                Log.d("MainActivity", "Parent directory is null for the file: ${file.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error performing file operations: ${e.message}", e)
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
        val localStorageDatasource = LocalStorageDatasource()
        val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
        if(initSetup!=null) {
            if(initSetup.autoStartApplication == "ON") {
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = intent
                    finish()
                    startActivity(intent)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }, 1000)
            }
        }
    }
}

class ScheduledTaskWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val portConnectionDatasource = PortConnectionDatasource()
    private val byteArrays = ByteArrays()
    override fun doWork(): Result {
        val taskName = inputData.getString("TASK_NAME")
        return when (taskName) {
            "TurnOnLightTask" -> {
                val localStorageDatasource = LocalStorageDatasource()
                val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
                Logger.debug("task scheduled turn on light")
                if(initSetup!=null) {
                    if(initSetup.autoTurnOnTurnOffLight=="ON") {
                        portConnectionDatasource.sendCommandVendingMachine(
                            byteArrays.vmTurnOnLight,
                        )
                    }
                }
                Result.success()
            }
            "TurnOffLightTask" -> {
                val localStorageDatasource = LocalStorageDatasource()
                val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
                Logger.debug("task scheduled turn off light")
                if(initSetup!=null) {
                    if(initSetup.autoTurnOnTurnOffLight=="ON") {
                        portConnectionDatasource.sendCommandVendingMachine(
                            byteArrays.vmTurnOffLight,
                        )
                    }
                }
                Result.success()
            }
            "ResetApp" -> {
                val localStorageDatasource = LocalStorageDatasource()
                val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
                if(initSetup != null) {
                    if(initSetup.autoResetAppEveryday=="ON") {
                        val appContext = applicationContext
                        restartApp(appContext)
                    }
                }
                Logger.debug("task scheduled reset app")
                Result.success()
            }
            else -> {
                Result.failure()
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val localStorageDatasource = LocalStorageDatasource()
        val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
        if(initSetup!=null) {
            if(initSetup.autoStartApplication == "ON") {
                if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
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