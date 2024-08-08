package com.combros.vendingmachine

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.combros.vendingmachine.core.util.pathFileInitSetup
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {
    private var appInForeground = true

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        registerActivityLifecycleCallbacks(this)
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
            restartApp()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    private fun restartApp() {
        val localStorageDatasource = LocalStorageDatasource()
        val initSetup = localStorageDatasource.getDataFromPath<InitSetup>(pathFileInitSetup)
        if(initSetup!=null) {
            if(initSetup.autoStartApplication == "ON") {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
}
