package com.leduytuanvu.vendingmachine

import android.app.Application
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.leduytuanvu.vendingmachine.core.util.Logger
//import com.leduytuanvu.vendingmachine.core.room.Graph
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//        Graph.provide(this)
        AndroidThreeTen.init(this)
        Thread.setDefaultUncaughtExceptionHandler(CustomExceptionHandler())
    }
}

class CustomExceptionHandler : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Logger.error("Error in my application")
    }
}