package com.leduytuanvu.vendingmachine.features.settings.presentation.view_model

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.models.LogException
//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.EventBus
import com.leduytuanvu.vendingmachine.core.util.exceptionHandling
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.view_state.SettingsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val settingRepository: SettingsRepository,
    private val localStorageDatasource: LocalStorageDatasource,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    init {
        loadSlotFromLocal()
        loadProductFromLocal()
        loadAndroidId()
    }

    private fun loadSlotFromLocal() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listSlot = settingRepository.initLoadSlotFromLocal()
                _state.update { it.copy(listSlot = listSlot) }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "loadListSlotFromLocal()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadProductFromLocal() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listProduct = settingRepository.loadListProductFromLocal()
                _state.update { it.copy(listProduct = listProduct) }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "loadListProductFromLocal()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadProductFromServer() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listProduct = settingRepository.loadProductFromServer()
                _state.update { it.copy(listProduct = listProduct) }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "loadListProductFromServer()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showDialogChooseNumber(
        isChooseMoney: Boolean = false,
        slot: Slot,
        isInventory: Boolean = false,
        isCapacity: Boolean = false,
    ) {
        viewModelScope.launch {
            if(isChooseMoney) {
                _state.update { it.copy(
                    isChooseMoney = true,
                    isCapacity = false,
                    isInventory = false,
                ) }
            }
            if(isCapacity) {
                _state.update { it.copy(
                    isChooseMoney = false,
                    isCapacity = true,
                    isInventory = false,
                ) }
            }
            if(isInventory) {
                _state.update { it.copy(
                    isChooseMoney = false,
                    isCapacity = false,
                    isInventory = true,
                ) }
            }
            _state.update { it.copy(
                isChooseNumber = true,
                slot = slot,
            ) }
        }
    }

    fun hideDialogChooseNumber() {
        viewModelScope.launch {
            _state.update { it.copy(
                isChooseNumber = false,
                isChooseMoney = false,
            ) }
        }
    }

    fun showDialogChooseImage(slot: Slot?) {
        viewModelScope.launch {
            if(slot != null) {
                _state.update { it.copy(
                    isChooseImage = true,
                    slot = slot
                ) }
            } else {
                _state.update { it.copy(isChooseImage = true) }
            }
        }
    }

    fun showMess(mess: String) {
        viewModelScope.launch {
            sendEvent(Event.Toast(mess))
        }
    }

    fun chooseNumber(number: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if(item.slot == _state.value.slot!!.slot) {
                        if(_state.value.isInventory) {
                            item.inventory = number
                        } else if(_state.value.isCapacity) {
                            item.capacity = number
                            if(_state.value.slot!!.inventory > number) {
                                item.inventory = number
                            }
                        } else {
                            item.price = number*1000
                        }
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "chooseNumber()")
            } finally {
                _state.update { it.copy(
                    isInventory = false,
                    isChooseNumber = false,
                    isChooseMoney = false,
                    isLoading = false
                ) }
            }
        }
    }

    fun addProductToListSlot(product: Product) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if(item.slot == _state.value.slot!!.slot) {
                        item.inventory = 10
                        item.capacity = 10
                        item.productCode = product.productCode!!
                        item.productName = product.productName!!
                        item.price = product.price!!
                        var slot: Slot? = null
                        for(itemAdd in _state.value.listSlotAddMore) {
                            if(itemAdd.slot == _state.value.slot!!.slot) {
                                slot = itemAdd
                            }
                        }
                        if(slot!=null) {
                            _state.value.listSlotAddMore.remove(slot)
                        }
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "addProductToListSlot()")
            } finally {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun addMoreProductToListSlot(product: Product) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(itemAdd in _state.value.listSlotAddMore) {
                    for(item in _state.value.listSlot) {
                        if(item.slot == itemAdd.slot) {
                            item.inventory = 10
                            item.capacity = 10
                            item.productCode = product.productCode!!
                            item.productName = product.productName!!
                            item.price = product.price!!
                            break
                        }
                    }
                }
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
                _state.value.listSlotAddMore.clear()
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "addMoreProductToListSlot()")
            } finally {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun fullInventory() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if(item.productCode.isNotEmpty()) {
                        item.inventory = item.capacity
                    }
                }
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "fullInventory()")
            } finally {
                _state.update { it.copy(
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun loadLayoutFromServer() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = true,
                    isConfirm = false,
                ) }
                val listSlot = settingRepository.loadLayoutFromServer()
                for(item in listSlot) {
                    val product = settingRepository.getProductByCode(item.productCode)
                    if(product!=null) {
                        item.price = product.price!!
                        item.productName = product.productName!!
                    }
                }
                _state.update { it.copy(listSlot = listSlot) }
                sendEvent(Event.Toast("SUCCESS"))
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "loadLayoutFromServer()")
            } finally {
                _state.update { it.copy(
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun hideDialogChooseImage() {
        viewModelScope.launch {
            _state.update { it.copy(isChooseImage = false) }
        }
    }

    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                isConfirm = true,
                titleConfirm = mess,
                slot = slot,
                nameFunction = nameFunction,
            ) }
        }
    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update { it.copy(
                slot = null,
                isConfirm = false,
                nameFunction = "",
            ) }
        }
    }

    fun addSlotToListAddMore(slot: Slot) {
        viewModelScope.launch {
            try {
                _state.value.listSlotAddMore.add(slot)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "addSlotToListAddMore()")
            }
        }
    }

    fun removeSlotToListAddMore(slot: Slot) {
        viewModelScope.launch {
            try {
                _state.value.listSlotAddMore.remove(slot)
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "addSlotToListAddMore()")
            }
        }

    }

    fun removeProduct() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if (item.slot == _state.value.slot!!.slot) {
                        item.inventory = 10
                        item.capacity = 10
                        item.productCode = ""
                        item.productName = "Not have product"
                        item.price = 10000
                        var check = false
                        for(itemAddMore in _state.value.listSlotAddMore) {
                            if(itemAddMore.slot == _state.value.slot!!.slot) {
                                check = true
                                break
                            }
                        }
                        if (check) _state.value.listSlotAddMore.remove(_state.value.slot)
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingRepository.writeListSlotToLocal(_state.value.listSlot)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "removeProduct()")
            } finally {
                _state.update { it.copy(
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun loadImageFromLocal(context: Context) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listImageBitmap = settingRepository.loadImageFromLocal(context)
                _state.update { it.copy(listImageProduct = listImageBitmap) }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "loadImageFromLocal()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun downloadProduct(context: Context) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = true,
                    isConfirm = false,
                ) }
                val folderImage = File(localStorageDatasource.folderImage)
                if (folderImage.exists()) {
                    localStorageDatasource.deleteFolder(folderImage)
                }
                localStorageDatasource.createFolder(localStorageDatasource.folderImage)
                for (product in state.value.listProduct) {
                    if(!product.imageUrl.isNullOrEmpty()) {
                        var notHaveError = true
                        for(i in 1..3) {
                            try {
                                val request = ImageRequest.Builder(context = context)
                                    .data(product.imageUrl)
                                    .build()
                                val result = withContext(Dispatchers.IO) {
                                    Coil.imageLoader(context).execute(request).drawable
                                }
                                if (result != null) {
                                    val file = File(localStorageDatasource.folderImage, "${product.productCode}.png")
                                    withContext(Dispatchers.IO) {
                                        file.outputStream().use { outputStream ->
                                            result.toBitmap().compress(Bitmap.CompressFormat.PNG, 1, outputStream)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                notHaveError = false
                            } finally {
                                if(notHaveError) break
                            }
                        }
                    }
                }
                localStorageDatasource.writeData(localStorageDatasource.fileProductDetail, localStorageDatasource.gson.toJson(state.value.listProduct))
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "downloadProduct()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }


    fun getLog(typeLog: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                var listLogException: ArrayList<LogException> = arrayListOf()
                var listLogExceptionSorted: ArrayList<LogException> = arrayListOf()
                if(localStorageDatasource.checkFileExists(localStorageDatasource.fileLogException)) {
                    val json = localStorageDatasource.readData(localStorageDatasource.fileLogException)
                    listLogException = localStorageDatasource.gson.fromJson(
                        json,
                        object : TypeToken<ArrayList<LogException>>() {}.type
                    ) ?: arrayListOf()
                    @SuppressLint("SimpleDateFormat")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    listLogExceptionSorted = ArrayList(listLogException.sortedByDescending {
                        dateFormat.parse(it.eventTime!!)
                    })
                }
                _state.update { it.copy(listLogException = listLogExceptionSorted) }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "getLog()")
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }


    fun loadAndroidId() {
        viewModelScope.launch {
            try {
                val androidId = settingRepository.getAndroidId(context = context)
                if(androidId.isNotEmpty()) {
                    _state.update { it.copy(androidId = androidId) }
                } else {
                    sendEvent(Event.Toast("Android id is empty!"))
                }
            } catch (e: Exception) {
                e.exceptionHandling(localStorageDatasource, exception = e, inFunction = "getAndroidId()")
            }
        }
    }
}