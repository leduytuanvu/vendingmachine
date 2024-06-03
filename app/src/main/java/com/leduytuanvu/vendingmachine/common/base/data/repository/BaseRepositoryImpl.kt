package com.leduytuanvu.vendingmachine.common.base.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.google.gson.Gson
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogAuthy
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogDepositWithdrawLocal
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogDoor
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogFill
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSensor
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogsLocal
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSpring
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogStatus
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogTemperature
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.pathFileLogDepositWithdrawServer
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
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
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
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
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
                gson.toJson(listLogServerLocal),
            )
            EventBus.sendEvent(Event.Toast(errorContent))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewSetupLogToLocal(
        machineCode: String,
        operationContent: String,
        operationType: String,
        username: String,
        severity: String
    ) {
        try {
            val logSetup = LogSetup(
                machineCode = machineCode,
                operationContent = operationContent,
                operationType = operationType,
                username = username,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "setup",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logSetup.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewAuthyLogToLocal(
        machineCode: String,
        authyType: String,
        username: String,
        severity: String
    ) {
        try {
            val logAuthy = LogAuthy(
                machineCode = machineCode,
                authyType = authyType,
                username = username,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "authy",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logAuthy.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewFillLogToLocal(
        machineCode: String,
        fillType: String,
        content: String,
        severity: String
    ) {
        try {
            val logFill = LogFill(
                machineCode = machineCode,
                fillType = fillType,
                content = content,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "fill",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logFill.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewSpringLogToLocal(
        machineCode: String,
        slot: Int,
        numberOfRevolutions: Int,
        severity: String
    ) {
        try {
            val logSpring = LogSpring(
                machineCode = machineCode,
                slot = slot,
                numberOfRevolutions = numberOfRevolutions,
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "spring",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logSpring.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewSensorLogToLocal(
        machineCode: String,
        cabinetCode: String,
        productCode: String,
        slot: String,
        status: String,
        severity: String
    ) {
        try {
            val logSensor  = LogSensor(
                machineCode = machineCode,
                cabinetCode = cabinetCode,
                productCode = productCode,
                slot = slot,
                status = status,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "sensor",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logSensor.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewTemperatureLogToLocal(
        machineCode: String,
        cabinetCode: String,
        currentTemperature: String,
        severity: String
    ) {
        try {
            val logTemperature  = LogTemperature(
                machineCode = machineCode,
                cabinetCode = cabinetCode,
                currentTemperature = currentTemperature,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "temperature",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logTemperature.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewDepositWithdrawLogToLocal(
        machineCode: String,
        transactionType: String,
        denominationType: Int,
        quantity: Int,
        currentBalance: Int
    ) {
        try {
            val logDepositWithdraw = LogDepositWithdrawLocal(
                vendCode = machineCode,
                transactionType = transactionType,
                denominationType = denominationType,
                quantity = quantity,
                currentBalance = currentBalance,
                synTime = LocalDateTime.now().toDateTimeString(),
                isSent = false,
            )
            var listLogDepositWithdraw = arrayListOf<LogDepositWithdrawLocal>()
            if (localStorageDatasource.checkFileExists(pathFileLogDepositWithdrawServer)) {
                listLogDepositWithdraw = localStorageDatasource.getDataFromPath(pathFileLogDepositWithdrawServer)!!
            }
            listLogDepositWithdraw.add(logDepositWithdraw)
            localStorageDatasource.writeData(
                pathFileLogDepositWithdrawServer,
                gson.toJson(listLogDepositWithdraw),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewStatusLogToLocal(
        machineCode: String,
        networkType: String,
        ip: String,
        networkStatus: String,
        powerInfo: String,
        severity: String,
    ) {
        try {
            val logStatus  = LogStatus(
                machineCode = machineCode,
                networkType = networkStatus,
                ip = ip,
                networkStatus = networkStatus,
                powerInfo = powerInfo,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "status",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logStatus.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun addNewDoorLogToLocal(
        machineCode: String,
        cabinetCode: String,
        operationType: String,
        severity: String
    ) {
        try {
            val logDoor  = LogDoor(
                machineCode = machineCode,
                cabinetCode = cabinetCode,
                operationType = operationType,
                eventTime = LocalDateTime.now().toDateTimeString(),
            )
            var listLogServerLocal = arrayListOf<LogsLocal>()
            val logServerLocal = LogsLocal (
                eventId = LocalDateTime.now().toId(),
                eventType = "door",
                severity = severity,
                eventTime = LocalDateTime.now().toDateTimeString(),
                eventData = logDoor.toBase64(),
                isSent = false,
            )
            if (localStorageDatasource.checkFileExists(pathFileLogServer)) {
                listLogServerLocal = localStorageDatasource.getDataFromPath(pathFileLogServer)!!
            }
            listLogServerLocal.add(logServerLocal)
            localStorageDatasource.writeData(
                pathFileLogServer,
                gson.toJson(listLogServerLocal),
            )
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

    override suspend fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                localStorageDatasource.deleteFile(file)
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

    override suspend fun isFileExists(pathFile: String) : Boolean {
        try {
            return localStorageDatasource.checkFileExists(pathFile)
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
        return try {
            byteArray.joinToString(",") { "%02X".format(it) }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun hexStringToByteArray(hexString: String): ByteArray {
        return try {
            hexString.split(",")
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } catch (e: Exception) {
            throw e
        }
    }
}