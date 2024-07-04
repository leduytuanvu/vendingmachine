package com.combros.vendingmachine

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
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.core.util.ByteArrays
import com.combros.vendingmachine.core.util.Navigation
import com.combros.vendingmachine.core.util.Event
import com.combros.vendingmachine.core.util.EventBus
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.ui.theme.VendingmachineTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.combros.vendingmachine.core.util.pathFileLogServer
import java.io.File
import android.os.Process
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var portConnectionDataSource: PortConnectionDatasource

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localStorageDatasource = LocalStorageDatasource()
        val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
        if(initSetup!=null) {
            initSetup.autoStartApplication = "ON"
            localStorageDatasource.writeData(pathFileInitSetup, localStorageDatasource.gson.toJson(initSetup))
        }

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
            Toast.makeText(this, "READ PHONE STATE permission is required to get SIM Serial ID", Toast.LENGTH_LONG).show()
        }
    }

    private val requestPermissionLauncherReadWriteExternalStorage = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission is granted, perform file operations
            performFileOperations()
        } else {
            // Permission denied, handle accordingly (e.g., show a message)
            Toast.makeText(this, "WRITE EXTERNAL STORAGE permission is required to save logs", Toast.LENGTH_LONG).show()
        }
    }

    private fun performFileOperations() {
        try {
            val file = File(pathFileLogServer)
            val parentDir = file.parentFile
            if (parentDir != null) {
                if (!parentDir.exists()) {
                    parentDir.mkdirs()
                }
                if (parentDir.exists()) {
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error performing file operations: ${e.message}", Toast.LENGTH_LONG).show()
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
                    Process.killProcess(Process.myPid())
                }, 1000)
            } else {
                closeApp(this)
            }
        }
    }
}

fun closeApp(activity: Activity) {
    activity.finishAffinity() // Close all activities
    Process.killProcess(Process.myPid()) // Kill the current process
    exitProcess(0) // Terminate the process
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
                        val intent = Intent(appContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        appContext.startActivity(intent)
                    }
                }
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