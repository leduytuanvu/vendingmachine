package com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type
import javax.inject.Inject

class LocalStorageDatasource @Inject constructor() {
    val gson = Gson()

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
            return gson.fromJson(json, type)
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

    fun getListFileNamesInFolder(folderPath: String): ArrayList<String> {
        try {
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                val fileList = folder.listFiles()
                if (fileList != null) {
                    return ArrayList(fileList.map { it.nameWithoutExtension } )
                }
            }
            return arrayListOf()
        } catch (e: Exception) {
            throw e
        }
    }

    fun getListPathFileInFolder(folderPath: String): ArrayList<String> {
        try {
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                val fileList = folder.listFiles()
                if (fileList != null) {
                    return ArrayList(fileList.map { it.absolutePath } )
                }
            }
            return arrayListOf()
        } catch (e: Exception) {
            throw e
        }
    }

    fun writeVideoAdsFromAssetToLocal(
        context: Context,
        rawResId: Int,
        fileName: String,
        pathFolderAds: String,
    ) {
        val dir = File(pathFolderAds)
        if (!dir.exists()) dir.mkdirs()
        val file = File(pathFolderAds, fileName)
        val inputStream: InputStream = context.resources.openRawResource(rawResId)
        val outputStream: OutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }
}