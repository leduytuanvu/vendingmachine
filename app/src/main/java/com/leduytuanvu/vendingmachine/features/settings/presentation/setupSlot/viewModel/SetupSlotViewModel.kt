package com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.home.data.model.request.ItemProductInventoryRequest
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Product
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.setupSlot.viewState.SetupSlotViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SetupSlotViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupSlotViewState())
    val state = _state.asStateFlow()

    fun loadInitSetupListSlotListProduct() {
        logger.debug("loadInitSetupListSlotListProduct")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                val listSlot = settingsRepository.getListSlotFromLocal()
                val listProduct = settingsRepository.getListProductFromLocal()
                _state.update {
                    it.copy(
                        initSetup = initSetup,
                        listSlot = listSlot,
                        listProduct = listProduct,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "load init setup list slot list product fail in SetupSlotViewModel/loadInitSetupListSlotListProduct: ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showToast(mess: String) {
        viewModelScope.launch {
            sendEvent(Event.Toast(mess))
        }
    }

    fun resetAllSlot() {
        viewModelScope.launch {
            try {
//                _state.update { it.copy(isLoading = true) }
//                baseRepository.addNewFillLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    fillType = "setup slot",
//                    content = "reset all slots"
//                )
                _state.update {
                    it.copy(
//                        titleDialogConfirm = "mess",
                        isConfirm = false,
//                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "reset all slot fail in SetupSlotViewModel/resetAllSlot(): ${e.message}",
                )
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

    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
        viewModelScope.launch {
            if(nameFunction == "resetAllSlot" || nameFunction == "setFullInventory" || nameFunction == "removeSlot") {
                _state.update { it.copy(
                    isConfirm = true,
                    titleDialogConfirm = mess,
                    slot = slot,
                    nameFunction = nameFunction,
                ) }
            } else {
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "add slot to state list add more fail in SetupSlotViewModel/addSlotToStateListAddMore(): ${e.message}",
                )
            }
        }
    }

    fun unlockSlot(slot: Slot) {
        logger.debug("unlockSlot")
        viewModelScope.launch {
            try {
                val listSlot = _state.value.listSlot
                val index = listSlot.indexOfFirst { it.slot == slot.slot }
                listSlot[index].isLock = false
                baseRepository.writeDataToLocal(listSlot, pathFileSlot)
                baseRepository.addNewFillLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    fillType = "setup slot",
                    content = "unlock slot ${slot.slot}"
                )
                _state.update { it.copy(listSlot = listSlot) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "add slot to state list add more fail in SetupSlotViewModel/addSlotToStateListAddMore(): ${e.message}",
                )
            }
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
                            val listSlotUpdateInventory = _state.value.listSlotUpdateInventory
                            val index = listSlotUpdateInventory.indexOfFirst { it.slot == item.slot }
                            if(index != -1) {
                                listSlotUpdateInventory.removeAt(index)
                            }
                            listSlotUpdateInventory.add(item)
                        } else if(_state.value.isCapacity) {
                            item.capacity = number
                            if(_state.value.slot!!.inventory > number) {
                                item.inventory = number
                            }
                        } else {
                            item.price = number*1000
                        }
                        baseRepository.addNewFillLogToLocal(
                            machineCode = _state.value.initSetup!!.vendCode,
                            fillType = "setup slot",
                            content = "setup number inventory, capacity or money for slot ${_state.value.slot!!.slot}"
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "setup number inventory, capacity or money fail in SetupSlotViewModel/chooseNumber(): ${e.message}",
                )
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

    fun hideDialogChooseNumber() {
        viewModelScope.launch {
            _state.update { it.copy(
                isChooseNumber = false,
                isChooseMoney = false,
            ) }
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "remove slot to state list add more fail in SetupSlotViewModel/removeSlotToStateListAddMore(): ${e.message}",
                )
            }
        }

    }

    fun removeSlot() {
        logger.debug("removeSlot")
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
                        baseRepository.addNewFillLogToLocal(
                            machineCode = _state.value.initSetup!!.vendCode,
                            fillType = "remove slot",
                            content = "remove slot ${_state.value.slot!!.slot}"
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "remove slot to local list slot fail in SetupSlotViewModel/removeSlot(): ${e.message}",
                )
            } finally {
                _state.update { it.copy (
                    nameFunction = "",
                    isConfirm = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun showDialogChooseImage(slot: Slot?) {
        viewModelScope.launch {
            if (slot != null) {
                _state.update {
                    it.copy(
                        isChooseImage = true,
                        slot = slot,
                    )
                }
            } else {
                _state.update { it.copy(
                    isChooseImage = true,
                    slot = null,
                ) }
            }
        }
    }

    fun goBack(navController: NavHostController) {
        viewModelScope.launch {
            try {
                val listUpdateInventory: ArrayList<ItemProductInventoryRequest> = arrayListOf()
                logger.debug("listUpdateInventory: ${listUpdateInventory.size}")
                for (item in _state.value.listSlotUpdateInventory) {
                    val itemUpdateInventory = ItemProductInventoryRequest(
                        cabinetCode = "MT01",
                        productLayoutId = "1",
                        slot = item.slot,
                        remaining = item.inventory,
                        isActive = 1,
                        id = item.slot.toString(),
                    )
                    listUpdateInventory.add(itemUpdateInventory)
                }
                if(listUpdateInventory.isNotEmpty()) {
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    baseRepository.addNewUpdateInventoryToLocal(
                        machineCode = initSetup.vendCode,
                        androidId = initSetup.androidId,
                        productList = listUpdateInventory,
                    )
                }
                navController.popBackStack()
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "go back fail in SetupSlotViewModel/goBack(): ${e.message}",
                )
            }
        }
    }

    fun setFullInventory() {
        logger.debug("setFullInventory")
        viewModelScope.launch {
            try {
                _state.update {
                    it.copy(
                        isLoading = true,
                        isConfirm = false
                    )
                }
                val listItemProductInventory = _state.value.listSlotUpdateInventory
                for (item in _state.value.listSlot) {
                    if (item.productCode.isNotEmpty()) {
                        item.inventory = item.capacity
                        val index = listItemProductInventory.indexOfFirst { it.slot == item.slot }
                        if(index != -1) {
                            listItemProductInventory.removeAt(index)
                        }
                        listItemProductInventory.add(item)
                    }
                }
                settingsRepository.writeListSlotToLocal(_state.value.listSlot)
                baseRepository.addNewFillLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    fillType = "setup slot",
                    content = "set full inventory for all slot",
                )
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "set full inventory for local list slot fail in SetupSlotViewModel/setFullInventory(): ${e.message}",
                )
            } finally {
                _state.update {
                    it.copy(
                        nameFunction = "",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun splitSlot(slot: Slot) {
        logger.debug("splitSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(1)
                val tmpListSlot = _state.value.listSlot
                for(index in 0..tmpListSlot.size) {
                    if(tmpListSlot[index].slot == slot.slot) {
                        tmpListSlot[index].isCombine = "no"
                        tmpListSlot[index].slotCombine = 0
                        tmpListSlot[index+1].status = 1
                        break
                    }
                }
                baseRepository.addNewFillLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    fillType = "setup slot",
                    content = "split slot ${slot.slot}",
                )
                baseRepository.writeDataToLocal(data = tmpListSlot, path = pathFileSlot)
                _state.update { it.copy (
                    listSlot = tmpListSlot,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "split slot fail in SetupSlotViewModel/splitSlot(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun mergeSlot(slot: Slot) {
        logger.debug("mergeSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(1)
                val tmpListSlot = _state.value.listSlot
                for(index in 0..tmpListSlot.size) {
                    if(tmpListSlot[index].slot == slot.slot) {
                        tmpListSlot[index].isCombine = "yes"
                        tmpListSlot[index].slotCombine = tmpListSlot[index].slot
                        tmpListSlot[index+1].status = 0
                        break
                    }
                }
                baseRepository.addNewFillLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    fillType = "setup slot",
                    content = "merge slot ${slot.slot}",
                )
                baseRepository.writeDataToLocal(data = tmpListSlot, path = pathFileSlot)
                _state.update { it.copy (
                    listSlot = tmpListSlot,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "merge slot fail in SetupSlotViewModel/mergeSlot(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
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
                        baseRepository.addNewFillLogToLocal(
                            machineCode = initSetup.vendCode,
                            fillType = "setup slot",
                            content = "add slot ${product.productCode} to slot ${_state.value.slot!!.slot}",
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "add slot fail in SetupSlotViewModel/addSlotToLocalListSlot(): ${e.message}",
                )
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
                            baseRepository.addNewFillLogToLocal(
                                machineCode = _state.value.initSetup!!.vendCode,
                                fillType = "setup slot",
                                content = "add more product ${product.productCode} to slot ${itemAdd.slot}",
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
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "add more slot fail in SetupSlotViewModel/addMoreProductToLocalListSlot(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = false,
                ) }
            }
        }
    }

    fun loadLayoutFromServer() {
        logger.debug("loadLayoutFromServer")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            isConfirm = false,
                        )
                    }
                    val listSlot = settingsRepository.getListLayoutFromServer()
                    for (item in listSlot) {
                        val product = settingsRepository.getProductByCodeFromLocal(item.productCode)
                        if (product != null) {
                            item.price = product.price
                            item.productName = product.productName
                        }
                    }
                    settingsRepository.writeListSlotToLocal(listSlot)
                    baseRepository.addNewFillLogToLocal(
                        machineCode = _state.value.initSetup!!.vendCode,
                        fillType = "load layout from server",
                        content = listSlot.toString(),
                    )
                    _state.update {
                        it.copy(
                            listSlot = listSlot,
                            nameFunction = "",
                            isLoading = false,
                        )
                    }
                    sendEvent(Event.Toast("SUCCESS"))
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "get layout from fail in SetupSlotViewModel/loadLayoutFromServer(): ${e.message}",
                )
                _state.update {
                    it.copy (
                        nameFunction = "",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun selectFunction() {
        viewModelScope.launch {
            when (state.value.nameFunction) {
                "resetAllSlot" -> resetAllSlot()
                "setFullInventory" -> setFullInventory()
                "loadLayoutFromServer" -> loadLayoutFromServer()
                "removeSlot" -> removeSlot()
            }
        }
    }

    private fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    titleDialogWarning = mess,
                    isWarning = true,
                )
            }
        }
    }

    fun hideDialogWarning() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isWarning = false,
                    titleDialogWarning = "",
                )
            }
        }
    }

    fun showDialogConfirm(mess: String, nameFunction: String) {
        viewModelScope.launch {
            if(nameFunction == "loadLayoutFromServer") {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update {
                        it.copy(
                            titleDialogConfirm = mess,
                            isConfirm = true,
                            nameFunction = nameFunction,
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            titleDialogWarning = "Not have internet, please connect with internet!",
                            isWarning = true,
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        titleDialogConfirm = mess,
                        isConfirm = true,
                        nameFunction = nameFunction,
                    )
                }
            }
        }
    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConfirm = false,
                    titleDialogConfirm = "",
                    nameFunction = "",
                )
            }
        }
    }

    fun hideDialogChooseImage() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isChooseImage = false,
                    slot = null,
                )
            }
        }
    }
}