package com.leduytuanvu.vendingmachine

import android.app.Application
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        Log.d("tuanvulog", "on create application")
        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
    }
}

class CustomExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e("tuanvulog", "${throwable.message}, ${throwable.localizedMessage}")
        // Handle the uncaught exception or error here
        // You can log the error, show a dialog to the user, or perform any other action
        // Note: Be cautious with error handling to avoid infinite loops or crashing loops
    }
}