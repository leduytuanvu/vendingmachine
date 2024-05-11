package com.leduytuanvu.vendingmachine.core.util

import android.util.Log

object Logger {
    private const val TAG = "VendingMachineApp"

    fun debug(message: String) {
        Log.d(TAG, message)
    }

    fun info(message: String) {
        Log.i(TAG, message)
    }

    fun warn(message: String) {
        Log.w(TAG, message)
    }

    fun error(message: String) {
        Log.e(TAG, message)
    }

    fun error(message: String, throwable: Throwable) {
        Log.e(TAG, message, throwable)
    }

    fun verbose(message: String) {
        Log.v(TAG, message)
    }
}