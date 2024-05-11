package com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject

class LocalStorageDatasource @Inject constructor() {
    val gson = Gson()

    @SuppressLint("SdCardPath")
    val fileInitSetup = "/sdcard/VendingMachineData/Setup/InitSetup.json"
    @SuppressLint("SdCardPath")
    val fileSlot = "/sdcard/VendingMachineData/Slot/Slot.json"
    @SuppressLint("SdCardPath")
    val fileProductDetail = "/sdcard/VendingMachineData/Product/ProductDetail.json"
    @SuppressLint("SdCardPath")
    val folderImage = "/sdcard/VendingMachineData/Product/Image"
    @SuppressLint("SdCardPath")
    val fileLogException = "/sdcard/VendingMachineData/Log/LogException.json"

    fun checkFileExists(path: String): Boolean {
        try {
            val file = File(path)
            return file.exists()
        } catch (e: Exception) {
            throw e
        }
    }

    fun checkFolderExists(path: String): Boolean {
        try {
            val folder = File(path)
            return folder.exists() && folder.isDirectory
        } catch (e: Exception) {
            throw e
        }
    }

    fun readData(path: String): String {
        try {
            var jsonData = ""
            val file = File(path)
            val fileInputStream = FileInputStream(file)
            jsonData = fileInputStream.readBytes().toString(Charsets.UTF_8)
            fileInputStream.close()
            return jsonData
        } catch (e: Exception) {
            throw e
        }
    }

    fun writeData(path: String, json: String): Boolean {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.use { stream ->
                stream.write(json.toByteArray())
            }
            return true
        } catch (e: IOException) {
            throw e
        }
    }

    inline fun <reified T> getDataFromPath(path: String): T? {
        try {
            val json = readData(path)
            val type: Type = object : TypeToken<T>() {}.type
            return Gson().fromJson(json, type)
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteFolder(folder: File) {
        try {
            if (folder.isDirectory) {
                val files = folder.listFiles()
                if (files != null) {
                    for (file in files) {
                        deleteFolder(file)
                    }
                }
            }
            folder.delete()
        } catch (e: Exception) {
            throw  e
        }
    }

    fun createFolder(folderPath: String): Boolean {
        try {
            val folder = File(folderPath)
            return if (!folder.exists()) {
                folder.mkdirs()
            } else {
                false
            }
        } catch (e: Exception) {
            throw e
        }
    }
}