package com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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

    fun listFileNamesInFolder(folderPath: String): ArrayList<String>? {
        try {
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                val fileList = folder.listFiles()
                if (fileList != null) {
                    return ArrayList(fileList.map { it.nameWithoutExtension } )
                }
            }
            return null
        } catch (e: Exception) {
            throw e
        }
    }

//    fun isImageExists(name: String): Boolean {
//        try {
//            val folder = File(pathFolderImage)
//            if (folder.exists() && folder.isDirectory) {
//                val fileList = folder.listFiles()
//                return if (fileList != null) {
//                    ArrayList(fileList.map { it.nameWithoutExtension })
//                } else {
//                    ArrayList()
//                }
//            }
//            return null
//        } catch (e: Exception) {
//            throw e
//        }
//    }
}