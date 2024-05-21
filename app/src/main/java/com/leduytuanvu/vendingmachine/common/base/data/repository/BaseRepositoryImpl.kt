package com.leduytuanvu.vendingmachine.common.base.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.google.gson.Gson
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServerLocal
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileLogServer
import com.leduytuanvu.vendingmachine.core.util.toBase64
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.core.util.toId
import org.threeten.bp.LocalDateTime
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject

class BaseRepositoryImpl @Inject constructor(
    private val localStorageDatasource: LocalStorageDatasource,
    private val gson: Gson,
    private val context: Context,
) : BaseRepository {
    override suspend fun <T> addNewLogToLocal(
        eventType: String,
        severity: String,
        eventData: T
    ) {
        try {
            var listLogServerLocal = arrayListOf<LogServerLocal>()
            val logServerLocal = LogServerLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = eventType,
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = eventData.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal)
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewErrorLogToLocal(
        machineCode: String,
        errorType: String,
        errorContent: String,
        severity: String
    ) {
        try {
            val logError = LogError(
                machineCode = machineCode,
                errorType = errorType,
                errorContent = errorContent,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogServerLocal>()
            val logServerLocal = LogServerLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "error",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logError.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal)
            )
            EventBus.sendEvent(Event.Toast(errorContent))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun isFileInitSetupExists() : Boolean {
        try {
            return localStorageDatasource.checkFileExists(pathFileInitSetup)
        } catch (e: Exception) {
            throw e
        }
    }

    @SuppressLint("HardwareIds")
    override suspend fun getAndroidId(): String {
        try {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun <T> getDataFromLocal(type: Type, path: String): T? {
        try {
            var data: T? = null
            if (localStorageDatasource.checkFileExists(path)) {
                val json = localStorageDatasource.readData(path)
                data = localStorageDatasource.gson.fromJson(json, type)
            }
            return data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun <T> writeDataToLocal(data: T, path: String) {
        try {
            localStorageDatasource.writeData(path, gson.toJson(data))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteFolder(path: String) {
        try {
            val folder = File(path)
            if (folder.exists()) {
                localStorageDatasource.deleteFolder(folder)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun createFolder(pathFolder: String) {
        try {
            localStorageDatasource.createFolder(pathFolder)
        } catch (e: Exception) {
            throw e
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun isHaveNetwork(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    capabilities?.run {
                        return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    } ?: false
                } else {
                    val networkInfo = connectivityManager.activeNetworkInfo
                    return networkInfo != null && networkInfo.isConnected
                }
            } else {
                false
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun isFolderExists(pathFolder: String): Boolean {
        return try {
            localStorageDatasource.checkFolderExists(pathFolder)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(",") { "%02X".format(it) }
    }

    override suspend fun hexStringToByteArray(hexString: String): ByteArray {
        return hexString.split(",")
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}