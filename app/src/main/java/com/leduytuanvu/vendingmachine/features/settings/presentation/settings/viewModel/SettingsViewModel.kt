package com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogFill
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogServerLocal
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileLogServer
import com.leduytuanvu.vendingmachine.core.util.pathFileProductDetail
import com.leduytuanvu.vendingmachine.core.util.pathFolderImage
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toDateTime
import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.settings.viewState.SettingsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.io.File
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SettingsViewModel @Inject constructor (
    private val settingsRepository: SettingsRepository,
    private val baseRepository: BaseRepository,
    private val portConnectionDataSource: PortConnectionDatasource,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    init {
//        getSlotFromLocal()
//        getProductFromLocal()
//        getInitSetupFromLocal()
//        getSerialSimId()
//        getInitInformationOfMachine()
    }

    fun getInitSetupFromLocal() {
        logger.debug("getInitSetupFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                _state.update { it.copy(initSetup = initSetup) }
            } catch (e: Exception) {
                sendEvent(Event.Toast(e.message!!))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getSlotFromLocal() {
        logger.debug("getSlotFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listSlot = settingsRepository.getListSlotFromLocal()
                _state.update { it.copy(listSlot = listSlot) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get slot from local fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getProductFromLocal() {
        logger.debug("getProductFromLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listProduct = settingsRepository.getListProductFromLocal()
                _state.update { it.copy(listProduct = listProduct) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get product from local fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // DONE
    fun getProductFromServer() {
        logger.debug("getProductFromServer")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listProduct = settingsRepository.getListProductFromServer()
                    _state.update { it.copy(listProduct = listProduct) }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get product from server fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
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
        logger.debug("showDialogChooseNumber")
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

    fun showToast(mess: String) {
        viewModelScope.launch {
            sendEvent(Event.Toast(mess))
        }
    }

    fun chooseNumber(number: Int) {
        logger.debug("chooseNumber")
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

                        val initSetup: InitSetup = baseRepository.getDataFromLocal(
                            type = object : TypeToken<InitSetup>() {}.type,
                            path = pathFileInitSetup
                        )!!
                        val logFill = LogFill(
                            machineCode = initSetup.vendCode,
                            fillType = "edit slot",
                            content = _state.value.slot.toString(),
                            eventTime = LocalDateTime.now().toDateTimeString(),
                        )
                        baseRepository.addNewLogToLocal(
                            eventType = "fill",
                            severity = "normal",
                            eventData = logFill,
                        )
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "choose number inventory, capacity or money fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
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

    fun addSlotToLocalListSlot(product: Product) {
        logger.debug("addSlotToLocalListSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if(item.slot == _state.value.slot!!.slot) {
                        item.inventory = 10
                        item.capacity = 10
                        item.productCode = product.productCode
                        item.productName = product.productName
                        item.price = product.price
                        var slot: Slot? = null
                        for(itemAdd in _state.value.listSlotAddMore) {
                            if(itemAdd.slot == _state.value.slot!!.slot) {
                                slot = itemAdd
                            }
                        }
                        if(slot!=null) {
                            _state.value.listSlotAddMore.remove(slot)
                        }

                        val initSetup: InitSetup = baseRepository.getDataFromLocal(
                            type = object : TypeToken<InitSetup>() {}.type,
                            path = pathFileInitSetup
                        )!!
                        val logFill = LogFill(
                            machineCode = initSetup.vendCode,
                            fillType = "add slot",
                            content = slot.toString(),
                            eventTime = LocalDateTime.now().toDateTimeString(),
                        )
                        baseRepository.addNewLogToLocal(
                            eventType = "fill",
                            severity = "normal",
                            eventData = logFill,
                        )
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "add slot to local list slot fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun addMoreProductToLocalListSlot(product: Product) {
        logger.debug("addMoreProductToLocalListSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(itemAdd in _state.value.listSlotAddMore) {
                    for(item in _state.value.listSlot) {
                        if(item.slot == itemAdd.slot) {
                            item.inventory = 10
                            item.capacity = 10
                            item.productCode = product.productCode
                            item.productName = product.productName
                            item.price = product.price

                            val initSetup: InitSetup = baseRepository.getDataFromLocal(
                                type = object : TypeToken<InitSetup>() {}.type,
                                path = pathFileInitSetup
                            )!!
                            val logFill = LogFill(
                                machineCode = initSetup.vendCode,
                                fillType = "add slot",
                                content = item.slot.toString(),
                                eventTime = LocalDateTime.now().toDateTimeString(),
                            )
                            baseRepository.addNewLogToLocal(
                                eventType = "fill",
                                severity = "normal",
                                eventData = logFill,
                            )
                            break
                        }
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
                _state.value.listSlotAddMore.clear()
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "add more slot to local list slot fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun setFullInventoryForLocalListSlot() {
        logger.debug("setFullInventoryForLocalListSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for(item in _state.value.listSlot) {
                    if(item.productCode.isNotEmpty()) {
                        item.inventory = item.capacity
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
                sendEvent(Event.Toast("SUCCESS"))
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logFill = LogFill(
                    machineCode = initSetup.vendCode,
                    fillType = "edit slot",
                    content = "set full inventory for all slot",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "fill",
                    severity = "normal",
                    eventData = logFill,
                )
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "set full inventory for local list slot fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun getLayoutFromServer() {
        logger.debug("getLayoutFromServer")
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = true,
                    isConfirm = false,
                ) }
                val listSlot = settingsRepository.getListLayoutFromServer()
                for(item in listSlot) {
                    val product = settingsRepository.getProductByCodeFromLocal(item.productCode)
                    if(product!=null) {
                        item.price = product.price
                        item.productName = product.productName
                    }
                }
                _state.update { it.copy(listSlot = listSlot) }
                sendEvent(Event.Toast("SUCCESS"))
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logFill = LogFill(
                    machineCode = initSetup.vendCode,
                    fillType = "get layout from server",
                    content = listSlot.toString(),
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "fill",
                    severity = "normal",
                    eventData = logFill,
                )
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get layout from fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
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
            if (baseRepository.isHaveNetwork(context)) {
                _state.update { it.copy(
                    isConfirm = true,
                    titleDialogConfirm = mess,
                    slot = slot,
                    nameFunction = nameFunction,
                ) }
            } else {
                showDialogWarning("Not have internet, please connect with internet!")
            }
        }
    }

    fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update { it.copy(
                isWarning = true,
                titleDialogWarning = mess,
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

    fun hideDialogWarning() {
        viewModelScope.launch {
            _state.update { it.copy(
                isWarning = false,
                titleDialogWarning = "",
            ) }
        }
    }

    fun addSlotToStateListAddMore(slot: Slot) {
        logger.debug("addSlotToStateListAddMore")
        viewModelScope.launch {
            try {
                _state.value.listSlotAddMore.add(slot)
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "add slot to state list add more fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun removeSlotToStateListAddMore(slot: Slot) {
        logger.debug("removeSlotToStateListAddMore")
        viewModelScope.launch {
            try {
                _state.value.listSlotAddMore.remove(slot)
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "remove slot to state list add more fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }

    }

    fun removeProductToLocalListSlot() {
        logger.debug("removeProductToLocalListSlot")
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
                        val initSetup: InitSetup = baseRepository.getDataFromLocal(
                            type = object : TypeToken<InitSetup>() {}.type,
                            path = pathFileInitSetup
                        )!!
                        val logFill = LogFill(
                            machineCode = initSetup.vendCode,
                            fillType = "remove slot",
                            content = item.toString(),
                            eventTime = LocalDateTime.now().toDateTimeString(),
                        )
                        baseRepository.addNewLogToLocal(
                            eventType = "fill",
                            severity = "normal",
                            eventData = logFill,
                        )
                        _state.update { it.copy(slot = null) }
                        break
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "remove slot to local list slot fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy (
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    // DONE
    fun downloadProductFromServer() {
        logger.debug("downloadProductFromServer")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context = context)) {
                    _state.update { it.copy(
                        isLoading = true,
                        isConfirm = false,
                    ) }
                    baseRepository.deleteFolder(pathFolderImage)
                    baseRepository.createFolder(pathFolderImage)
                    for (product in state.value.listProduct) {
                        if(product.imageUrl!!.isNotEmpty()) {
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
                                        val file = File(pathFolderImage, "${product.productCode}.png")
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
                    baseRepository.writeDataToLocal(
                        data = state.value.listProduct,
                        path = pathFileProductDetail,
                    )
                    sendEvent(Event.Toast("SUCCESS"))
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    val logFill = LogFill(
                        machineCode = initSetup.vendCode,
                        fillType = "download product from server",
                        content = state.value.listProduct.toString(),
                        eventTime = LocalDateTime.now().toDateTimeString(),
                    )
                    baseRepository.addNewLogToLocal(
                        eventType = "fill",
                        severity = "normal",
                        eventData = logFill,
                    )
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "download product from server fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getInitInformationOfMachine() {
        logger.debug("getInitInformationOfMachine")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    val informationOfMachine = settingsRepository.getInformationOfMachine()
                    _state.update { it.copy(informationOfMachine = informationOfMachine) }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get init information machine from server fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun getInformationOfMachine() {
        logger.debug("getInformationOfMachine")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val informationOfMachine = settingsRepository.getInformationOfMachine()
                _state.update { it.copy(informationOfMachine = informationOfMachine) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get information machine from server fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getSerialSimId() {
        logger.debug("getSerialSimId")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val serialSimId = settingsRepository.getSerialSimId()
                _state.update { it.copy(serialSimId = serialSimId) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "get serial sim fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveSetupPort(
        typeVendingMachine: String,
        portCashBox: String,
        portVendingMachine: String,
    ) {
        logger.debug("saveSetupPort")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                if(portCashBox == portVendingMachine) {
                    sendEvent(Event.Toast("Port cash box and port vending machine must not same!"))
                } else {
                    delay(1000)
                    if (portConnectionDataSource.openPortCashBox(portCashBox) == -1) {
                        throw Exception("Open port cash box is error!")
                    } else {
                        portConnectionDataSource.startReadingCashBox()
                    }
                    if (portConnectionDataSource.openPortVendingMachine(portVendingMachine) == -1) {
                        throw Exception("Open port vending machine is error!")
                    } else {
                        portConnectionDataSource.startReadingVendingMachine()
                    }
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    initSetup.typeVendingMachine = typeVendingMachine
                    initSetup.portCashBox = portCashBox
                    initSetup.portVendingMachine = portVendingMachine
                    baseRepository.writeDataToLocal(
                        data = initSetup,
                        path = pathFileInitSetup,
                    )
                    val logSetup = LogSetup(
                        machineCode = initSetup.vendCode,
                        operationContent = "set: $initSetup",
                        operationType = "setup port",
                        username = initSetup.username,
                        eventTime = LocalDateTime.now().toDateTimeString(),
                    )
                    baseRepository.addNewLogToLocal(
                        eventType = "setup",
                        severity = "normal",
                        eventData = logSetup,
                    )
                    sendEvent(Event.Toast("Setup port success"))
                    _state.update { it.copy (
                        initSetup = initSetup,
                        isLoading = false
                    ) }
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val logError = LogError(
                    machineCode = initSetup.vendCode,
                    errorType = "application",
                    errorContent = "save setup port fail: ${e.message}",
                    eventTime = LocalDateTime.now().toDateTimeString(),
                )
                baseRepository.addNewLogToLocal(
                    eventType = "error",
                    severity = "normal",
                    eventData = logError,
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getAllLogServerLocal() {
        logger.debug("getAllLogServerLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listLogServerLocal: ArrayList<LogServerLocal> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogServerLocal>>() {}.type,
                    path = pathFileLogServer
                )!!
                listLogServerLocal.sortByDescending { it.eventTime.toDateTime() }
                _state.update { it.copy(listLogServerLocal = listLogServerLocal) }
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateVendCodeInLocal(newVendCode: String) {
        logger.debug("updateVendCodeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.vendCode = newVendCode
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateFullScreenAdsInLocal(fullScreenAds: String) {
        logger.debug("updateFullScreenAdsInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.fullScreenAds = fullScreenAds
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateWithdrawalAllowedInLocal(withdrawalAllowed: String) {
        logger.debug("updateWithdrawalAllowedInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.withdrawalAllowed = withdrawalAllowed
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateAutoStartApplicationInLocal(autoStartApplication: String) {
        logger.debug("updateAutoStartApplicationInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.autoStartApplication = autoStartApplication
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateLayoutHomeInLocal(layoutHomeScreen: String) {
        logger.debug("updateLayoutHomeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.layoutHomeScreen = layoutHomeScreen
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTimeTurnOnTurnOffLightInLocal(timeTurnOnLight: String, timeTurnOffLight: String) {
        logger.debug("updateTimeTurnOnTurnOffLightInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.timeTurnOnLight = timeTurnOnLight
                initSetup.timeTurnOffLight = timeTurnOffLight
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateDropSensorInLocal(dropSensor: String) {
        logger.debug("updateDropSensorInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.dropSensor = dropSensor
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateInchingModeInLocal(inchingMode: String) {
        logger.debug("updateInchingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.inchingMode = inchingMode
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTimeJumpToAdsScreenInLocal(timeToJumpToAdsScreen: String) {
        logger.debug("updateTimeJumpToAdsScreenInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.timeToJumpToAdsScreen = timeToJumpToAdsScreen
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateGlassHeatingModeInLocal(glassHeatingMode: String) {
        logger.debug("updateGlassHeatingModeInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.glassHeatingMode = glassHeatingMode
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateHighestTempWarningInLocal(highestTempWarning: String) {
        logger.debug("updateHighestTempWarningInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.highestTempWarning = highestTempWarning
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateLowestTempWarningInLocal(lowestTempWarning: String) {
        logger.debug("updateLowestTempWarningInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.lowestTempWarning = lowestTempWarning
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTemperatureInLocal(temperature: String) {
        logger.debug("updateTemperatureInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.temperature = temperature
                _state.update { it.copy(initSetup = initSetup) }
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun check() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDataSource.sendCommandVendingMachine(ByteArrays().vmCheckDropSensor)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}