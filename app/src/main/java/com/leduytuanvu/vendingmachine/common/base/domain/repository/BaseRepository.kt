package com.leduytuanvu.vendingmachine.common.base.domain.repository

import android.content.Context
import java.lang.reflect.Type

interface BaseRepository {
    suspend fun <T> addNewLogToLocal(
        eventType: String,
        severity: String,
        eventData: T,
    )
//    suspend fun getInitSetupFromLocal() : InitSetup
    suspend fun isFileInitSetupExists() : Boolean
    suspend fun getAndroidId() : String
//    suspend fun writeInitSetupToLocal(initSetup: InitSetup)
    suspend fun <T> writeDataToLocal(data: T, path: String)
    suspend fun <T> getDataFromLocal(type: Type, path: String): T?
    suspend fun deleteFolder(path: String)
    suspend fun createFolder(pathFolder: String)
    suspend fun isHaveNetwork(context: Context): Boolean
    suspend fun isFolderExists(pathFolder: String): Boolean
}