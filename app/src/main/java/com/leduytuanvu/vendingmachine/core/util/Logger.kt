package com.leduytuanvu.vendingmachine.core.util

import android.util.Log
import com.leduytuanvu.vendingmachine.features.base.domain.model.LogServerLocal
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class Logger @Inject constructor () {
    private val tag = "vuldt"

    fun debug(message: String) {
        Log.d(tag, message)
    }

    fun info(message: String) {
        Log.i(tag, message)
    }

    fun warn(message: String) {
        Log.w(tag, message)
    }

    fun error(message: String) {
        Log.e(tag, message)
    }

    fun error(message: String, throwable: Throwable) {
        Log.e(tag, message, throwable)
    }

    fun <T> addNewLogToLocalLogServerLocal(
        eventType: String,
        severity: String,
        eventData: T?,
        localStorageDatasource: LocalStorageDatasource,
    ) {
        try {
            var listLogServerLocal = arrayListOf<LogServerLocal>()
            val logServerLocal = LogServerLocal (
                event_id = LocalDateTime.now().toId(),
                event_type = eventType,
                severity = severity,
                event_time = LocalDateTime.now().toDateTimeString(),
                event_data = eventData.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(pathFileLogServer, localStorageDatasource.gson.toJson(listLogServerLocal))
        } catch (e: Exception) {
            error(e.message!!)
        }
    }
}