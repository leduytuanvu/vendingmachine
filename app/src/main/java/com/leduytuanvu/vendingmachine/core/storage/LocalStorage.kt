package com.leduytuanvu.vendingmachine.core.storage

import android.annotation.SuppressLint
import android.os.Environment
import com.google.gson.Gson
import java.io.File

class LocalStorage {
    private val gson = Gson()

    @SuppressLint("SdCardPath")
    val filePathVendCode = "/sdcard/AVFData/json/vendcode.json"

    fun checkFileExists(filePath: String): Boolean {
        try {
            val file = File(filePath)
            return file.exists()
        } catch (e: Exception) {
            throw e
        }
    }

    fun checkFolderExists(folderPath: String): Boolean {
        try {
            val folder = File(folderPath)
            return folder.exists() && folder.isDirectory
        } catch (e: Exception) {
            throw e
        }
    }
}