package com.leduytuanvu.vendingmachine.common.base.domain.repository

import android.content.Context
import java.lang.reflect.Type

interface BaseRepository {
    suspend fun <T> addNewLogToLocal(
        eventType: String,
        severity: String,
        eventData: T,
    )
    suspend fun addNewErrorLogToLocal(
        machineCode: String,
        errorType: String = "application",
        errorContent: String,
        severity: String = "normal",
    )
    suspend fun addNewSetupLogToLocal(
        machineCode: String,
        operationContent: String,
        operationType: String,
        username: String,
        severity: String = "normal",
    )
    suspend fun addNewAuthyLogToLocal(
        machineCode: String,
        authyType: String,
        username: String,
        severity: String = "normal",
    )
    suspend fun addNewFillLogToLocal(
        machineCode: String,
        fillType: String,
        content: String,
        severity: String = "normal",
    )
    suspend fun addNewSpringLogToLocal(
        machineCode: String,
        slot: Int,
        numberOfRevolutions: Int,
        severity: String = "normal",
    )
    suspend fun addNewSensorLogToLocal(
        machineCode: String,
        cabinetCode: String,
        productCode: String,
        slot: String,
        status: String,
        severity: String = "normal",
    )
    suspend fun addNewTemperatureLogToLocal(
        machineCode: String,
        cabinetCode: String,
        currentTemperature: String,
        severity: String = "normal",
    )
    suspend fun addNewDepositWithdrawLogToLocal(
        machineCode: String,
        transactionType: String,
        denominationType: Int,
        quantity: Int = 1,
        currentBalance: Int,
    )
    suspend fun addNewStatusLogToLocal(
        machineCode: String,
        networkType: String,
        ip: String,
        networkStatus: String,
        powerInfo: String,
        severity: String = "normal",
    )
    suspend fun getAndroidId() : String
    suspend fun isHaveNetwork(context: Context): Boolean
    suspend fun <T> writeDataToLocal(data: T, path: String)
    suspend fun <T> getDataFromLocal(type: Type, path: String): T?
    suspend fun deleteFolder(path: String)
    suspend fun deleteFile(path: String)
    suspend fun createFolder(pathFolder: String)
    suspend fun isFileExists(pathFile: String) : Boolean
    suspend fun isFolderExists(pathFolder: String): Boolean
    suspend fun byteArrayToHexString(byteArray: ByteArray): String
    suspend fun hexStringToByteArray(hexString: String): ByteArray
}