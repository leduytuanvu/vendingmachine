package com.combros.vendingmachine.features.settings.presentation.setupSlot.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository

import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.TypeRXCommunicateAvf
import com.combros.vendingmachine.core.util.ByteArrays

import com.combros.vendingmachine.core.util.Event
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.core.util.pathFileSlot
import com.combros.vendingmachine.core.util.sendEvent

import com.combros.vendingmachine.features.home.data.model.request.ItemProductInventoryRequest
import com.combros.vendingmachine.features.home.presentation.viewModel.DropSensorResult

import com.combros.vendingmachine.features.settings.domain.model.Product
import com.combros.vendingmachine.features.settings.domain.model.Slot
import com.combros.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.combros.vendingmachine.features.settings.presentation.setupSlot.viewState.SetupSlotViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.math.log

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SetupSlotViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val baseRepository: BaseRepository,
    private val byteArrays: ByteArrays,
    private val logger: Logger,
    private val context: Context,

) : ViewModel() {
    private val _state = MutableStateFlow(SetupSlotViewState())
    val state = _state.asStateFlow()
    private var vendingMachineJob: Job? = null
    var listSlotAll: ArrayList<Slot> = arrayListOf()

    private val _statusSlot = MutableStateFlow(false)
    val statusSlot: StateFlow<Boolean> = _statusSlot.asStateFlow()

//    private val _isRotate = MutableStateFlow(false)
//    val isRotate: StateFlow<Boolean> = _isRotate.asStateFlow()

    private val _checkFirst = MutableStateFlow(false)
    val checkFirst: StateFlow<Boolean> = _checkFirst.asStateFlow()

    private val _isSetUpVendingMachine = MutableStateFlow(false)
    val isSetUpVendingMachine: StateFlow<Boolean> = _isSetUpVendingMachine.asStateFlow()

    private val _isRotate = MutableStateFlow(false)
    val isRotate: StateFlow<Boolean> = _isRotate.asStateFlow()

    private val _checkSetupVendingMachine = MutableStateFlow(false)
    val checkSetupVendingMachine: StateFlow<Boolean> = _checkSetupVendingMachine.asStateFlow()

    private val _statusDropProduct = MutableStateFlow(DropSensorResult.ANOTHER)
    val statusDropProduct: StateFlow<DropSensorResult> = _statusDropProduct.asStateFlow()

    fun loadInitSetupListSlotListProduct() {
        logger.debug("loadInitSetupListSlotListProduct")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
//                logger.debug("1")
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
//                logger.debug("2")
                portConnectionDatasource.openPortVendingMachine(initSetup.portVendingMachine,initSetup.typeVendingMachine)
                if(!portConnectionDatasource.checkPortVendingMachineStillStarting()) {
                    portConnectionDatasource.startReadingVendingMachine()
                }
//                logger.debug("3")
//                portConnectionDatasource.startReadingVendingMachine()
                startCollectingData()
//                logger.debug("4")
                val listSlot = settingsRepository.getListSlotFromLocal()
                val listProduct = settingsRepository.getListProductFromLocal()
//                logger.debug("5")
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

    fun showDialogUpdateNumberSlot(numberSlot: String) {
        viewModelScope.launch {
            try {
                if(numberSlot.toInt()>300 || numberSlot.toInt() < 0) {
                    sendEvent(Event.Toast("Number slot must from 0 to 300"))
                } else {
                    _state.update { it.copy(
                        numberSlot = numberSlot,
                        isConfirm = true,
                        nameFunction = "updateNumberSlot",
                        titleDialogConfirm = "It will reset all slots. Are you sure you want to update number slots?",
                    ) }
                }
            } catch (e: Exception) {
                sendEvent(Event.Toast("Number slot must be a number!"))
            }
        }
    }

    fun updateNumberSlotInLocal() {
        logger.debug("updateNumberSlotInLocal")
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isLoading = true,
                    isConfirm = false,
                ) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                initSetup.numberSlot = _state.value.numberSlot.toInt()
                val listSlot: ArrayList<Slot> = arrayListOf()
                for(i in 1..initSetup.numberSlot) {
                    listSlot.add(
                        Slot(
                            slot = i,
                            productCode = "",
                            productName = "",
                            inventory = 6,
                            capacity = 6,
                            price = 10000,
                            isCombine = "no",
                            springType = "lo xo don",
                            status = 1,
                            slotCombine = 0,
                            isLock = false,
                            isEnable = true,
                            messDrop = "",
                        )
                    )
                }
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "update number slot to ${initSetup.vendCode}",
                    operationType = "setup number slot",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                baseRepository.writeDataToLocal(data = listSlot, path = pathFileSlot)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    listSlot = listSlot,
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup,
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "update number slot fail in SetupSystemViewModel/updateNumberSlotInLocal(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetAllSlot() {
        logger.debug("resetAllSlot")
        viewModelScope.launch {
            try {
                _isSetUpVendingMachine.value = true
                _state.update { it.copy(
                    isConfirm = false,
                    isLoading = true,
                ) }
                _checkSetupVendingMachine.value = false
                sendResetAllSlotToSingle(0)
                delay(1001)
                if(_checkSetupVendingMachine.value) {
                    val listSlot = arrayListOf<Slot>()
                    // Get init setup in local
                    val initSetup: InitSetup = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )!!
                    for(i in 1..initSetup.numberSlot) {
                        listSlot.add(
                            Slot(
                                slot = i,
                                productCode = "",
                                productName = "",
                                inventory = 6,
                                capacity = 6,
                                price = 10000,
                                isCombine = "no",
                                springType = "lo xo don",
                                status = 1,
                                slotCombine = 0,
                                isLock = false,
                                isEnable = true,
                                messDrop = "",
                            )
                        )
                    }
                    baseRepository.writeDataToLocal(listSlot, pathFileSlot)
                    sendEvent(Event.Toast("SUCCESS"))
                    _state.update {
                        it.copy(
                            listSlot = listSlot,
                            isLoading = false,
                        )
                    }
                } else {
                    sendEvent(Event.Toast("FAIL"))
                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                }
//                baseRepository.addNewFillLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    fillType = "setup slot",
//                    content = "reset all slots"
//                )
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
            } finally {
                _isSetUpVendingMachine.value = false
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
            if (isChooseMoney) {
                _state.update {
                    it.copy(
                        isChooseMoney = true,
                        isCapacity = false,
                        isInventory = false,
                    )
                }
            }
            if (isCapacity) {
                _state.update {
                    it.copy(
                        isChooseMoney = false,
                        isCapacity = true,
                        isInventory = false,
                    )
                }
            }
            if (isInventory) {
                _state.update {
                    it.copy(
                        isChooseMoney = false,
                        isCapacity = false,
                        isInventory = true,
                    )
                }
            }
            _state.update {
                it.copy(
                    isChooseNumber = true,
                    slot = slot,
                )
            }
        }
    }

    fun showDialogConfirm(mess: String, slot: Slot?, nameFunction: String) {
        viewModelScope.launch {
            if (nameFunction == "resetAllSlot" || nameFunction == "setFullInventory" || nameFunction == "removeSlot") {
                _state.update {
                    it.copy(
                        isConfirm = true,
                        titleDialogConfirm = mess,
                        slot = slot,
                        nameFunction = nameFunction,
                    )
                }
            } else {
                if (baseRepository.isHaveNetwork(context)) {
                    _state.update {
                        it.copy(
                            isConfirm = true,
                            titleDialogConfirm = mess,
                            slot = slot,
                            nameFunction = nameFunction,
                        )
                    }
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

    fun unlockSlot(slot: Slot, onSuccess: () -> Unit) {
        logger.debug("unlockSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                _isRotate.value = true
                _checkFirst.value = true
                val byteArraySlot: Byte = slot.slot.toByte()
                val byteArrayNumberBoard: Byte = 0.toByte()
                val byteArray: ByteArray = byteArrayOf(
                    byteArrayNumberBoard,
                    (0xFF - 0).toByte(),
                    byteArraySlot,
                    (0xFF - slot.slot).toByte(),
                    0xAA.toByte(),
                    0x55,
                )
                productDispense(0, slot.slot)
                val listSlot = _state.value.listSlot
                val index = listSlot.indexOfFirst { it.slot == slot.slot }
                listSlot[index].isLock = false
                baseRepository.writeDataToLocal(listSlot, pathFileSlot)
                baseRepository.addNewFillLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    fillType = "setup slot",
                    content = "unlock slot ${slot.slot}"
                )
                onSuccess()
                _state.update { it.copy(listSlot = listSlot,isLoading = false,) }
//                _state.update { it.copy(isLoading = false) }
//                _statusDropProduct.value =
//                    DropSensorResult.INITIALIZATION
//                portConnectionDatasource.sendCommandVendingMachine(byteArray)
//                var result = withTimeoutOrNull(20000L) {
//                    statusDropProduct.first { it != DropSensorResult.INITIALIZATION }
//                }
//                if(result == null) {
//                    sendEvent(Event.Toast("Unlock slot fail! (TIMEOUT)"))
//                } else {
//                    val listSlot = _state.value.listSlot
//                    val index = listSlot.indexOfFirst { it.slot == slot.slot }
//                    listSlot[index].isLock = false
//                    baseRepository.writeDataToLocal(listSlot, pathFileSlot)
//                    baseRepository.addNewFillLogToLocal(
//                        machineCode = _state.value.initSetup!!.vendCode,
//                        fillType = "setup slot",
//                        content = "unlock slot ${slot.slot}"
//                    )
//                    onSuccess()
//                    _state.update { it.copy(listSlot = listSlot) }
//                }
//                _state.update { it.copy(isLoading = false) }
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
                for (item in _state.value.listSlot) {
                    if (item.slot == _state.value.slot!!.slot) {
                        if (_state.value.isInventory) {
                            item.inventory = number
                            val listSlotUpdateInventory = _state.value.listSlotUpdateInventory
                            val index = listSlotUpdateInventory.indexOfFirst { it.slot == item.slot }
                            if(index != -1) {
                                listSlotUpdateInventory.removeAt(index)
                            }
                            listSlotUpdateInventory.add(item)
                        } else if(_state.value.isCapacity) {
                            item.capacity = number
                            if (_state.value.slot!!.inventory > number) {
                                item.inventory = number
                            }
                        } else {
//                            item.price = number * 1000
                            for (itemTmp in _state.value.listSlot) {
                                if(item.productCode == itemTmp.productCode) {
                                    logger.info("product slot == ${itemTmp.slot}")
                                    itemTmp.price = number * 1000
                                }
                            }

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
                _state.update {
                    it.copy(
                        isInventory = false,
                        isChooseNumber = false,
                        isChooseMoney = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun hideDialogChooseNumber() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isChooseNumber = false,
                    isChooseMoney = false,
                )
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
                for (item in _state.value.listSlot) {
                    if (item.slot == _state.value.slot!!.slot) {
                        item.inventory = 6
                        item.capacity = 6
                        item.productCode = ""
                        item.productName = "Not have product"
                        item.price = 10000
                        var check = false
                        for (itemAddMore in _state.value.listSlotAddMore) {
                            if (itemAddMore.slot == _state.value.slot!!.slot) {
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
                _state.update {
                    it.copy(
                        nameFunction = "",
                        isConfirm = false,
                        isLoading = false,
                    )
                }
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
                _state.update {
                    it.copy(
                        isChooseImage = true,
                        slot = null,
                    )
                }
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
                _isSetUpVendingMachine.value = true
                _state.update { it.copy(isLoading = true) }
                delay(1)
                val tmpListSlot = _state.value.listSlot
                val copiedList = ArrayList(tmpListSlot)
                for (index in 0..copiedList.size) {
                    if (copiedList[index].slot == slot.slot) {
                        _checkSetupVendingMachine.value = false
                        sendSplitSlot(0,slot.slot)
                        delay(1001)
                        if(_checkSetupVendingMachine.value) {
                            copiedList[index].isCombine = "no"
                            copiedList[index].slotCombine = 0
                            copiedList[index + 1].status = 1
                            baseRepository.addNewFillLogToLocal(
                                machineCode = _state.value.initSetup!!.vendCode,
                                fillType = "setup slot",
                                content = "split slot ${slot.slot}",
                            )
                            baseRepository.writeDataToLocal(data = copiedList, path = pathFileSlot)
                            sendEvent(Event.Toast("SUCCESS"))
                            _state.update {
                                it.copy(
                                    listSlot = copiedList,
                                    isLoading = false,
                                )
                            }
                        } else {
                            sendEvent(Event.Toast("FAIL"))
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                )
                            }
                        }
                        break
                    }
                }
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
            } finally {
                _isSetUpVendingMachine.value = false
            }
        }
    }

    public suspend fun sendSplitSlot(numberBoard: Int = 0, startSlot: Int) {
        val byteNumberBoard: Byte = numberBoard.toByte()
        val byteStartSlot: Byte = startSlot.toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                byteNumberBoard,
                (0xFF - numberBoard).toByte(),
                0xC9.toByte(),
                0x36,
                byteStartSlot,
                (0xFF - startSlot).toByte(),
            )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

    public suspend fun sendMergeSlot(numberBoard: Int = 0, startSlot: Int) {
        val byteNumberBoard: Byte = numberBoard.toByte()
        val byteStartSlot: Byte = startSlot.toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                byteNumberBoard,
                (0xFF - numberBoard).toByte(),
                0xCA.toByte(),
                0x35,
                byteStartSlot,
                (0xFF - startSlot).toByte(),
            )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

    public suspend fun sendResetAllSlotToSingle(numberBoard: Int = 0) {
        val byteNumberBoard: Byte = numberBoard.toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                byteNumberBoard,
                (0xFF - numberBoard).toByte(),
                0xCB.toByte(),
                0x34,
                0X55,
                0XAA.toByte(),
            )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

    fun mergeSlot(slot: Slot) {
        logger.debug("mergeSlot")
        viewModelScope.launch {
            try {
                _isSetUpVendingMachine.value = true
                _state.update { it.copy(isLoading = true) }
                delay(1)
                val tmpListSlot = _state.value.listSlot
                val copiedList = ArrayList(tmpListSlot)
                for (index in 0..copiedList.size) {
                    if (copiedList[index].slot == slot.slot) {
                        _checkSetupVendingMachine.value = false
                        sendMergeSlot(0,slot.slot)
                        delay(1001)
                        if(_checkSetupVendingMachine.value) {
                            copiedList[index].isCombine = "yes"
                            copiedList[index].slotCombine = copiedList[index].slot
                            copiedList[index + 1].status = 0
                            baseRepository.addNewFillLogToLocal(
                                machineCode = _state.value.initSetup!!.vendCode,
                                fillType = "setup slot",
                                content = "merge slot ${slot.slot}",
                            )
                            baseRepository.writeDataToLocal(data = copiedList, path = pathFileSlot)
                            sendEvent(Event.Toast("SUCCESS"))
                            _state.update {
                                it.copy(
                                    listSlot = copiedList,
                                    isLoading = false,
                                )
                            }
                        } else {
                            logger.debug("merge vô else")
                            sendEvent(Event.Toast("FAIL"))
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                )
                            }
                        }
                        break
                    }
                }
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
            } finally {
                _isSetUpVendingMachine.value = false
            }
        }
    }

    fun addSlotToLocalListSlot(product: Product) {
        logger.debug("addSlotToLocalListSlot: product: ${product}")
        logger.debug("listSlot: ${_state.value.listSlot}")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                for (item in _state.value.listSlot) {
                    if (item.slot == _state.value.slot!!.slot) {
                        item.inventory = 6
                        item.capacity = 6
                        val indexCheck = _state.value.listSlot.indexOfFirst { it.productCode == product.productCode }
                        logger.debug("index: ${indexCheck}")
                        if(indexCheck!=-1) {
                            item.price = _state.value.listSlot[indexCheck].price
                        } else {
                            item.price = product.price
                        }
                        item.productCode = product.productCode
                        item.productName = product.productName
                        var slot: Slot? = null
                        for (itemAdd in _state.value.listSlotAddMore) {
                            if (itemAdd.slot == _state.value.slot!!.slot) {
                                slot = itemAdd
                            }
                        }
                        if (slot != null) {
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
                _state.update {
                    it.copy(
                        isChooseImage = false,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun addMoreProductToLocalListSlot(product: Product) {
        logger.debug("addMoreProductToLocalListSlot")
        viewModelScope.launch {
            try {
                _state.update { it.copy(
                    isChooseImage = false,
                    isLoading = true,
                ) }
                withContext(Dispatchers.IO) {
                    for (itemAdd in _state.value.listSlotAddMore) {
                        for (item in _state.value.listSlot) {
                            if (item.slot == itemAdd.slot) {
                                item.inventory = 6
                                item.capacity = 6
                                val indexCheck = _state.value.listSlot.indexOfFirst { it.productCode == product.productCode }
                                item.productCode = product.productCode
                                item.productName = product.productName
                                if(indexCheck!=-1) {
                                    item.price = _state.value.listSlot[indexCheck].price
                                } else {
                                    item.price = product.price
                                }
                                baseRepository.addNewFillLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    fillType = "setup slot",
                                    content = "add more product ${product.productCode} to slot ${itemAdd.slot}",
                                )
                                break
                            }
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
                _state.update {
                    it.copy(
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun loadLayoutFromServer() {
        logger.debug("loadLayoutFromServer")
        viewModelScope.launch {
            try {
                if (baseRepository.isHaveNetwork(context)) {
                    _state.update {
                        it.copy(
                            isLoading = true,
                            isConfirm = false,
                        )
                    }
                    if(!portConnectionDatasource.checkPortVendingMachineStillStarting()) {
                        portConnectionDatasource.startReadingVendingMachine()
                    }
                    portConnectionDatasource.startReadingVendingMachine()
                    val listSlotFromServer = settingsRepository.getListLayoutFromServer()
                    val listSlotFromLocal = settingsRepository.getListSlotFromLocal()
                    logger.info("Size list slot from server: ${listSlotFromServer.size}, size list slot in local: ${listSlotFromLocal.size}")
                    logger.info(listSlotFromServer.toString())
                    var maxSlotFromServer = 0
                    for (item in listSlotFromServer) {
                        if (item.slot > maxSlotFromServer) {
                            maxSlotFromServer = item.slot
                        }
                    }
                    if(listSlotFromServer.size>listSlotFromLocal.size || maxSlotFromServer>listSlotFromLocal.size) {
                        sendEvent(Event.Toast("Size list slot from server is ${listSlotFromServer.size} and max slot from server is ${maxSlotFromServer}, please choose number slot is ${listSlotFromServer.size} and number slot is ${maxSlotFromServer}"))
                    } else {
                        for (itemSlotServer in listSlotFromServer) {
                            for (itemSlotLocal in listSlotFromLocal) {
                                if(itemSlotServer.slot == itemSlotLocal.slot) {
                                    logger.info("slot get from server = ${itemSlotServer.slot}")
                                    val product = settingsRepository.getProductByCodeFromLocal(itemSlotServer.productCode)
                                    logger.info("Product: ${product}")
                                    if (product != null) {
                                        itemSlotLocal.price = product.price
                                        itemSlotLocal.productName = product.productName
                                        itemSlotLocal.productCode = product.productCode
                                    }
                                    break
                                }
                            }
                        }
                        settingsRepository.writeListSlotToLocal(listSlotFromLocal)
                        baseRepository.addNewFillLogToLocal(
                            machineCode = _state.value.initSetup!!.vendCode,
                            fillType = "load layout from server",
                            content = listSlotFromLocal.toString(),
                        )
                        sendEvent(Event.Toast("SUCCESS"))
                    }
                    _state.update {
                        it.copy(
                            listSlot = listSlotFromLocal,
                            nameFunction = "",
                            isLoading = false,
                        )
                    }
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
                    it.copy(
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
                "updateNumberSlot" -> updateNumberSlotInLocal()
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
            if (nameFunction == "loadLayoutFromServer") {
                if (baseRepository.isHaveNetwork(context)) {
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

    fun closePort() {
        vendingMachineJob?.cancel()
        vendingMachineJob = null
        portConnectionDatasource.closeVendingMachinePort()
    }

    fun startCollectingData() {
        vendingMachineJob = viewModelScope.launch {
            portConnectionDatasource.dataFromVendingMachine.collect { data ->
//                logger.debug("data: ${baseRepository.byteArrayToHexString(data)}")
                processingDataFromVendingMachine(data)
            }
        }
    }

    fun processingDataFromVendingMachine(dataByteArray: ByteArray) {
//        if(_checkFirst.value) {
            val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
            if(_isSetUpVendingMachine.value) {
                if(dataHexString=="00,5D,00,00,5D") {
                    logger.debug("set up success")
                    _checkSetupVendingMachine.value = true
                }
            }
            if(_isRotate.value) {
                logger.debug("data receive from vending machine: $dataHexString")
                if(dataHexString=="00,5D,01,00,5E" || dataHexString=="00,5C,00,00,5C") {
                    logger.debug("status door")
                } else {
                    val result = when (dataHexString) {
                        "00,5D,00,00,5D" -> DropSensorResult.ROTATED_BUT_PRODUCT_NOT_FALL
                        "00,5D,00,AA,07" -> DropSensorResult.SUCCESS
                        "00,5C,40,00,9C" -> DropSensorResult.NOT_ROTATED
                        "00,5C,02,00,5E" -> DropSensorResult.NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM
                        "00,5D,00,CC,29" -> DropSensorResult.ROTATED_BUT_INSUFFICIENT_ROTATION
                        "00,5D,00,33,90" -> DropSensorResult.ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED
                        "00,5C,03,00,5F" -> DropSensorResult.SENSOR_HAS_AN_OBSTACLE
                        "00,5C,50,00,AC" -> DropSensorResult.ERROR_00_5C_50_00_AC_PRODUCT_NOT_FALL
                        "00,5C,50,AA,56" -> DropSensorResult.ERROR_00_5C_50_AA_56_PRODUCT_FALL
                        else -> DropSensorResult.INITIALIZATION
                    }
                    if (result != DropSensorResult.INITIALIZATION) {
                        _statusDropProduct.tryEmit(result)
                        logger.debug("Result emitted to dispenseResults: $result")
                        sendEvent(Event.Toast(result.name))
                    }

//                    when (dataHexString) {
//                        "00,5D,00,00,5D" -> sendEvent(Event.Toast("ROTATED_BUT_PRODUCT_NOT_FALL"))
//                        "00,5D,00,AA,07" -> sendEvent(Event.Toast("SUCCESS"))
//                        "00,5C,40,00,9C" -> sendEvent(Event.Toast("NOT_ROTATED"))
//                        "00,5C,02,00,5E" -> sendEvent(Event.Toast("NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"))
//                        "00,5D,00,CC,29" -> sendEvent(Event.Toast("ROTATED_BUT_INSUFFICIENT_ROTATION"))
//                        "00,5D,00,33,90" -> sendEvent(Event.Toast("ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED"))
//                        "00,5C,03,00,5F" -> sendEvent(Event.Toast("SENSOR_HAS_AN_OBSTACLE"))
//                        "00,5C,50,00,AC" -> sendEvent(Event.Toast("ERROR_00_5C_50_00_AC_PRODUCT_NOT_FALL"))
//                        "00,5C,50,AA,56" -> sendEvent(Event.Toast("ERROR_00_5C_50_AA_56_PRODUCT_FALL"))
//                        else -> sendEvent(Event.Toast("UNKNOWN_ERROR_${dataHexString}"))
//                    }
//                    _statusDropProduct.tryEmit("result")
                }
                _isRotate.value = false
            }


//        }
//        if (dataHexString.contains("00,5D,00,00,5D")) {
//            _statusSlot.value = true
////            resultadd(true)
//
//        } else if (dataHexString.contains("00,5C,40,00,9C")) {
//            sendEvent(Event.Toast("NOT_FOUND_THIS_SLOT"))
//        }
    }

    fun enquirySlot(
        numberBoard: Int = 0,
        slot: Int,
    ) {
        val byteArraySlot: Byte = (slot + 120).toByte()
        val byteArrayNumberBoard: Byte = numberBoard.toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                byteArrayNumberBoard,
                (0xFF - numberBoard).toByte(),
                byteArraySlot,
                (0x86 - (slot - 1)).toByte(),
                0x55,
                0xAA.toByte(),
            )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

    fun productDispenseNotSensor(
        numberBoard: Int = 0,
        slot: Int,
    ) {
        val byteArraySlot: Byte = slot.toByte()
        val byteArrayNumberBoard: Byte = numberBoard.toByte()
        val byteArray: ByteArray = byteArrayOf(
            byteArrayNumberBoard,
            (0xFF - numberBoard).toByte(),
            byteArraySlot,
            (0xFF - slot).toByte(),
            0x55,
            0xAA.toByte(),
        )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

    fun productDispense(
        numberBoard: Int = 0,
        slot: Int,
    ) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                _isRotate.value = true
                _checkFirst.value = true
                val byteArraySlot: Byte = slot.toByte()
                val byteArrayNumberBoard: Byte = numberBoard.toByte()
                val byteArray: ByteArray = byteArrayOf(
                    byteArrayNumberBoard,
                    (0xFF - numberBoard).toByte(),
                    byteArraySlot,
                    (0xFF - slot).toByte(),
                    0xAA.toByte(),
                    0x55,
                )
                _statusDropProduct.value =
                    DropSensorResult.INITIALIZATION
                portConnectionDatasource.sendCommandVendingMachine(byteArray)
                logger.debug("1")
                var result = withTimeoutOrNull(20000L) {
                    statusDropProduct.first { it != DropSensorResult.INITIALIZATION }
                }
                logger.debug("2")
                if(result == null) {
                    logger.debug("3")
                    sendEvent(Event.Toast("TIME_OUT"))
                }
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                sendEvent(Event.Toast("ERROR"))
            }
        }
    }


    private suspend fun updateSlotEnable(list: ArrayList<Slot>) {
        viewModelScope.launch {
            try {
                var resultList = list
                repeat(list.size) { index ->
                    val resultEnquirySlot = portConnectionDatasource.enquirySlot(
                        slot =
                        list[index].slot,
                    )
                    if(resultEnquirySlot.typeRXCommunicateAvf == TypeRXCommunicateAvf.SUCCESS){
                        resultList[index].isEnable = true
                    }else if(resultEnquirySlot.typeRXCommunicateAvf == TypeRXCommunicateAvf.SLOT_NOT_FOUND){
                        resultList[index].isEnable = false
                        _state.value.initSetup?.let { baseRepository.addNewErrorLogToLocal(machineCode = it.vendCode,errorContent = "Slot ${resultList[index].slot} not working") }
                    }
                }
                _state.update { it -> it.copy(listSlot = resultList.filter { it.isEnable } as ArrayList<Slot>) }
            } catch (e:Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "${e.message}"
                )
            }
        }
    }
}