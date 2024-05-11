package com.leduytuanvu.vendingmachine.features.settings.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.models.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toSlot
import com.leduytuanvu.vendingmachine.features.settings.data.remote.SettingsApi
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.update
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsApi: SettingsApi
) : SettingsRepository {
    private val localStorageDatasource = LocalStorageDatasource()
    override suspend fun initLoadSlotFromLocal(): ArrayList<Slot> {
        try {
            var listSlot = arrayListOf<Slot>()
            if(localStorageDatasource.checkFileExists(localStorageDatasource.fileSlot)) {
                val type: Type = object : TypeToken<ArrayList<Slot>>() {}.type
                val json = localStorageDatasource.readData(localStorageDatasource.fileSlot)
                listSlot = localStorageDatasource.gson.fromJson(json, type)
            } else {
                for(i in 1..60) {
                    listSlot.add(
                        Slot(
                            slot = i,
                            productCode = "",
                            productName = "",
                            inventory = 10,
                            capacity = 10,
                            price = 10000,
                            isCombine = "no",
                            springType = "lo xo don",
                            status = 1,
                            slotCombine = 0,
                            isLock = false
                        )
                    )
                }
                localStorageDatasource.writeData(localStorageDatasource.fileSlot, localStorageDatasource.gson.toJson(listSlot))
            }
            return listSlot
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun loadLayoutFromServer(): ArrayList<Slot> {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(localStorageDatasource.fileInitSetup) ?: return arrayListOf()
            val response = settingsApi.loadLayout(initSetup.vendCode)
            val listSlot: ArrayList<Slot> = arrayListOf()
            for(item in response.data!!) {
                listSlot.add(item.slot.toSlot())
            }
            return listSlot
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun loadProductFromServer(): ArrayList<Product> {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(localStorageDatasource.fileInitSetup) ?: return arrayListOf()
            val response = settingsApi.loadProduct(initSetup.vendCode)
            val listTmp: ArrayList<Product> = arrayListOf()
            for (item in response.data!!) {
                if (item.imageUrl.isNullOrEmpty() || item.imageUrl.contains(".jfif")) {
                    listTmp.add(item)
                }
            }
            for (item in listTmp) {
                response.data.remove(item)
            }
            return response.data
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun loadListProductFromLocal(): ArrayList<Product> {
        try {
            var listProduct = arrayListOf<Product>()
            if(localStorageDatasource.checkFileExists(localStorageDatasource.fileProductDetail)) {
                val type: Type = object : TypeToken<ArrayList<Product>>() {}.type
                val json = localStorageDatasource.readData(localStorageDatasource.fileProductDetail)
                listProduct = localStorageDatasource.gson.fromJson(json, type)
            }
            return listProduct
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>): Boolean {
        try {
            return localStorageDatasource.writeData(localStorageDatasource.fileSlot, localStorageDatasource.gson.toJson(listSlot))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun loadImageFromLocal(context: Context): ArrayList<ImageBitmap> {
        try {
            val folder = File(localStorageDatasource.folderImage)
            val imageBitmapList = ArrayList<ImageBitmap>()
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        bitmap?.let {
                            imageBitmapList.add(it.asImageBitmap())
                        }
                    }
                }
            } else {
                EventBus.sendEvent(Event.Toast("Not have any image product!"))
            }
            return imageBitmapList
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getProductByCode(productCode: String): Product? {
        try {
            var listProduct = arrayListOf<Product>()
            if(localStorageDatasource.checkFileExists(localStorageDatasource.fileProductDetail)) {
                val type: Type = object : TypeToken<ArrayList<Product>>() {}.type
                val json = localStorageDatasource.readData(localStorageDatasource.fileProductDetail)
                listProduct = localStorageDatasource.gson.fromJson(json, type)
            }
            for(item in listProduct) {
                if(item.productCode == productCode) {
                    return item
                }
            }
            return null
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getAndroidId(context: Context): String {
        try {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            throw e
        }
    }
}