package com.leduytuanvu.vendingmachine

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
//import com.leduytuanvu.vendingmachine.core.room.Graph
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    private var appInForeground = true

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        registerActivityLifecycleCallbacks(this)
//        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->
            restartApp()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        appInForeground = false
    }

    override fun onActivityResumed(activity: Activity) {
        appInForeground = true
    }

    override fun onActivityStopped(activity: Activity) {
        if (!appInForeground) {
            val localStorageDatasource = LocalStorageDatasource()
            val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
            if(initSetup!=null) {
                if(initSetup.autoStartApplication == "ON") {
                    restartApp()
                }
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    // Other lifecycle methods
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}

class CustomExceptionHandler() : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Logger.error("Error in my application")
    }
}