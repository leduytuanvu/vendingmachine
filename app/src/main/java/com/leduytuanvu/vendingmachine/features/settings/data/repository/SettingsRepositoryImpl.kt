package com.leduytuanvu.vendingmachine.features.settings.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.telephony.TelephonyManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.features.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileProductDetail
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.toSlot
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.DataInformationMachineResponse
import com.leduytuanvu.vendingmachine.features.settings.data.remote.SettingsApi
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsApi: SettingsApi,
    private val localStorageDatasource: LocalStorageDatasource,
) : SettingsRepository {

    override suspend fun getListSlotFromLocal(): ArrayList<Slot> {
        try {
            var listSlot = arrayListOf<Slot>()
            if(localStorageDatasource.checkFileExists(pathFileSlot)) {
                val type: Type = object : TypeToken<ArrayList<Slot>>() {}.type
                val json = localStorageDatasource.readData(pathFileSlot)
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
                localStorageDatasource.writeData(pathFileSlot, localStorageDatasource.gson.toJson(listSlot))
            }
            return listSlot
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListLayoutFromServer(): ArrayList<Slot> {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(pathFileInitSetup) ?: return arrayListOf()
            val response = settingsApi.getLayout(initSetup.vendCode)
            val listSlot = arrayListOf<Slot>()
            for(item in response.data!!) {
                listSlot.add(item.slot.toSlot())
            }
            return listSlot
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListProductFromServer(): ArrayList<Product> {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(pathFileInitSetup) ?: return arrayListOf()
            val response = settingsApi.getListProduct(initSetup.vendCode)
            val listTmp = arrayListOf<Product>()
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

    override suspend fun getListProductFromLocal(): ArrayList<Product> {
        try {
            var listProduct = arrayListOf<Product>()
            if(localStorageDatasource.checkFileExists(pathFileProductDetail)) {
                val type: Type = object : TypeToken<ArrayList<Product>>() {}.type
                val json = localStorageDatasource.readData(pathFileProductDetail)
                listProduct = localStorageDatasource.gson.fromJson(json, type)
            }
            return listProduct
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun writeListSlotToLocal(listSlot: ArrayList<Slot>): Boolean {
        try {
            return localStorageDatasource.writeData(pathFileSlot, localStorageDatasource.gson.toJson(listSlot))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun writeInitSetupToLocal(initSetup: InitSetup): Boolean {
        try {
            return localStorageDatasource.writeData(pathFileInitSetup, localStorageDatasource.gson.toJson(initSetup))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getListImageBitmapFromLocal(context: Context): ArrayList<ImageBitmap> {
        try {
            val folder = File(pathFolderImage)
            val imageBitmapList = ArrayList<ImageBitmap>()
            if (localStorageDatasource.checkFolderExists(pathFolderImage)) {
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

    override suspend fun getProductByCodeFromLocal(productCode: String): Product? {
        try {
            var listProduct = arrayListOf<Product>()
            if(localStorageDatasource.checkFileExists(pathFileProductDetail)) {
                val type: Type = object : TypeToken<ArrayList<Product>>() {}.type
                val json = localStorageDatasource.readData(pathFileProductDetail)
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

    override suspend fun getInformationOfMachine(): DataInformationMachineResponse {
        try {
            val initSetup: InitSetup = localStorageDatasource.getDataFromPath(pathFileInitSetup)!!
            val response = settingsApi.getInformationOfMachine(initSetup.vendCode)
            return response.data!!
        } catch (e: Exception) {
            throw e
        }
    }

    @SuppressLint("HardwareIds")
    override suspend fun getSerialSimId(context: Context): String {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                throw Exception("Not have permission!")
            }
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.simSerialNumber
            } else {
                telephonyManager.simSerialNumber
            }
        } catch (e: Exception) {
            throw e
        }
    }
}