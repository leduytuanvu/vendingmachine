package com.combros.vendingmachine.features.settings.presentation.setupPayment.viewModel

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.Coil
import coil.request.ImageRequest
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.ScheduledTaskWorker
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.core.util.ByteArrays
import com.combros.vendingmachine.core.util.Event
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.core.util.pathFilePaymentMethod
import com.combros.vendingmachine.core.util.pathFolderImagePayment
import com.combros.vendingmachine.core.util.sendEvent
import com.combros.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.combros.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.combros.vendingmachine.features.settings.presentation.setupPayment.viewState.SetupPaymentViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SetupPaymentViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val baseRepository: BaseRepository,
    private val logger: Logger,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupPaymentViewState())
    val state = _state.asStateFlow()
    private val workManager = WorkManager.getInstance(context)

    private var cashBoxJob: Job? = null
    private var vendingMachineJob: Job? = null

    private val _processCashBoxSuccess = MutableStateFlow(false)
    val processCashBoxSuccess: StateFlow<Boolean> = _processCashBoxSuccess.asStateFlow()

//    private val _returnMoneySuccess = MutableStateFlow(false)
//    val returnMoneySuccess: StateFlow<Boolean> = _returnMoneySuccess.asStateFlow()

    private val _dispensedBill = MutableStateFlow(false)
    val dispensedBill: StateFlow<Boolean> = _dispensedBill.asStateFlow()

    fun loadInitData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                // Open port and start reading
                portConnectionDatasource.openPortCashBox(initSetup.portCashBox)
                if(!portConnectionDatasource.checkPortCashBoxStillStarting()) {
                    portConnectionDatasource.startReadingCashBox()
                }
                portConnectionDatasource.openPortVendingMachine(initSetup.portVendingMachine,initSetup.typeVendingMachine)
                if(!portConnectionDatasource.checkPortVendingMachineStillStarting()) {
                    portConnectionDatasource.startReadingVendingMachine()
                }
                // Start collecting data
                startCollectingData()
                // Get current cash
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
                // List payment method
                val listPaymentMethod: ArrayList<PaymentMethodResponse>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<PaymentMethodResponse>>() {}.type,
                    path = pathFilePaymentMethod
                )
                _state.update { it.copy(
                    initSetup = initSetup,
                    listPaymentMethod = listPaymentMethod ?: arrayListOf(),
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewErrorLogToLocal(
                    machineCode = initSetup.vendCode,
                    errorContent = "load init data fail in HomeViewModel/loadInitData(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun turnOnPutMoneyInTheRottenBox() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(putMoneyInTheRottenBox = true) }
            } catch (e: Exception) {
                logger.debug("turn on fail")
            }
        }
    }

    fun turnOffPutMoneyInTheRottenBox(callback: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(putMoneyInTheRottenBox = false) }
                callback()
            } catch (e: Exception) {
                logger.debug("turn on fail")
            }
        }
    }

    fun saveDefaultPromotion(defaultPromotion: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.initPromotion = defaultPromotion
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "save default promotion ${defaultPromotion}",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "save default promotion fail in SetupPaymentViewModel/saveDefaultPromotion(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveSetTimeResetOnEveryDay(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.timeResetOnEveryDay = "$hour:$minute"
                rescheduleDailyTask("ResetAppTask", hour, minute)
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "save time reset on every day ${hour}:${minute}",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "save default promotion fail in SetupPaymentViewModel/saveDefaultPromotion(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun rescheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        try {
            workManager.cancelUniqueWork(taskName).also {
                Logger.debug("Delete task scheduled $taskName and make scheduled again")
                scheduleDailyTask(taskName, hour, minute)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private fun scheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        try {
            val currentTime = Calendar.getInstance()
            val targetTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(currentTime)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis

            val inputData = Data.Builder()
                .putString("TASK_NAME", taskName)
                .build()

            val dailyWorkRequest = PeriodicWorkRequestBuilder<ScheduledTaskWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            workManager.enqueueUniquePeriodicWork(
                taskName,
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun saveTimeoutPaymentQrCode(timeoutPaymentQrCode: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val regex = "\\d+".toRegex()
                val timeoutPaymentQrCodeTmp = regex.find(timeoutPaymentQrCode)?.value
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.timeoutPaymentByQrCode = timeoutPaymentQrCodeTmp!!
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "save timeout payment qr code ${timeoutPaymentQrCodeTmp}",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "save timeout payment qr code fail in SetupViewModel/saveTimeoutPaymentQrCode(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun closePort() {
        cashBoxJob?.cancel()
        cashBoxJob = null
        vendingMachineJob?.cancel()
        vendingMachineJob = null
        portConnectionDatasource.closeCashBoxPort()
    }

    fun saveTimeoutPaymentCash(timeoutPaymentCash: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                val regex = "\\d+".toRegex()
                val timeoutPaymentCashTmp = regex.find(timeoutPaymentCash)?.value
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.timeoutPaymentByCash = timeoutPaymentCashTmp!!
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "save timeout payment cash ${timeoutPaymentCashTmp}",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "save timeout payment qr code fail in SetupViewModel/saveTimeoutPaymentQrCode(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshCurrentCash() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                delay(500)
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                initSetup.currentCash = 0
                baseRepository.addNewSetupLogToLocal(
                    machineCode = initSetup.vendCode,
                    operationContent = "refresh current cash",
                    operationType = "setup payment",
                    username = initSetup.username,
                )
                baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(
                    initSetup = initSetup,
                    isLoading = false,
                ) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "refresh current cash fail in HomeViewModel/refreshCurrentCash(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refreshRottenBoxBalance() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
                delay(500)
                sendEvent(Event.Toast("SUCCESS"))
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "refresh current cash fail in HomeViewModel/refreshCurrentCash(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun startCollectingData() {
        cashBoxJob = viewModelScope.launch {
            portConnectionDatasource.dataFromCashBox.collect { data ->
                processingDataFromCashBox(data)
            }
        }
        vendingMachineJob = viewModelScope.launch {
            portConnectionDatasource.dataFromVendingMachine.collect { data ->
                processingDataFromVendingMachine(data)
            }
        }
    }

    private fun processingCash(cash: Int) {
        viewModelScope.launch {
            try {
                if(cash == 10000) {
                    portConnectionDatasource.sendCommandCashBox(ByteArrays().cbStack)
                } else {
                    portConnectionDatasource.sendCommandCashBox(ByteArrays().cbReject)
                }
            } catch (e: Exception) {
                logger.debug("error: ${e.message}")
            }
        }

    }

    fun pollStatus() {
//        logger.debug("pollStatus")
        viewModelScope.launch {
            portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
        }
    }

    fun processingDataFromCashBox(dataByteArray: ByteArray) {
        try {
            if(dataByteArray.isNotEmpty()) {
                val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
                logger.debug("data from cash box ======== "+dataHexString)
//                if(dataHexString == "01,00,03,00,01,03") {
//                    _returnMoneySuccess.value = true
//                }
                if(_dispensedBill.value) {
                    if(dataHexString == "01,00,03,00,02,00") {
                        sendEvent(Event.Toast("Rotten box balance is 0"))
                    }
                }

                if (dataByteArray.size == 19){
                    if (dataByteArray[6] == 0x00.toByte()) {
                        when (dataByteArray[7]) {
                            0x04.toByte() -> {
                                processingCash(10000)
                            }
                            0x03.toByte(), 0x02.toByte(), 0x01.toByte(), 0x05.toByte(), 0x06.toByte(), 0x07.toByte(), 0x08.toByte(), 0x09.toByte() -> {
                                processingCash(0)
                            }
                            else -> {
                                logger.debug("data from cash box ======== "+dataHexString)
                            }
                        }
                    }
                }
                if(dataHexString.contains("01,01,03,00,00,")) {
                    // Define the byte to balance map
                    val byteToBalanceMap = mapOf(
                        0x01.toByte() to 1,
                        0x02.toByte() to 2,
                        0x03.toByte() to 3,
                        0x04.toByte() to 4,
                        0x05.toByte() to 5,
                        0x06.toByte() to 6,
                        0x07.toByte() to 7,
                        0x08.toByte() to 8,
                        0x09.toByte() to 9,
                        0x0A.toByte() to 10,
                        0x0B.toByte() to 11,
                        0x0C.toByte() to 12,
                        0x0D.toByte() to 13,
                        0x0E.toByte() to 14,
                        0x0F.toByte() to 15,
                        0x10.toByte() to 16,
                        0x11.toByte() to 17,
                        0x12.toByte() to 18,
                        0x13.toByte() to 19,
                        0x14.toByte() to 20,
                        0x15.toByte() to 21,
                        0x16.toByte() to 22,
                        0x17.toByte() to 23,
                        0x18.toByte() to 23,
                        0x19.toByte() to 24,
                        0x1A.toByte() to 25,
                        0x1B.toByte() to 26,
                        0x1C.toByte() to 27,
                        0x1D.toByte() to 28,
                        0x1E.toByte() to 29,
                        0x1F.toByte() to 30,
                        0x20.toByte() to 31,
                        0x21.toByte() to 32,
                        0x22.toByte() to 33,
                        0x23.toByte() to 34
                    )
                    val byteArray = hexStringToByteArray(dataHexString)
                    // Get the value from the map or default to 0 if not found
                    val numberRottenBoxBalance = byteToBalanceMap.getOrDefault(byteArray[5], 0)
                    // Update the state
                    logger.debug("numberRottenBoxBalance: $numberRottenBoxBalance")
                    _state.update { it.copy(numberRottenBoxBalance = numberRottenBoxBalance) }
                    _processCashBoxSuccess.value = true
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun processingDataFromVendingMachine(dataByteArray: ByteArray) {
        try {
            val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
            logger.debug("data from vending machine: "+dataHexString)
        } catch (e: Exception) {
            throw e
        }
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

    fun dispensedOne() {
        viewModelScope.launch {
            try {
                _dispensedBill.value = true
//                _state.update { it.copy(isLoading = true) }
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbDispenseBill1)
                delay(300)
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
                delay(300)
                _dispensedBill.value = false
//                delay(300)
//                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                logger.error("dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}")
//                baseRepository.addNewErrorLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    errorContent = "dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}",
//                )
            }
        }
    }

    fun transferToCashBox() {
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
                delay(300)
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbTransferToCashBox)
                delay(10000)
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
            } catch (e: Exception) {
                logger.error("dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}")
//                baseRepository.addNewErrorLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    errorContent = "dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}",
//                )
            }
        }
    }

    fun stack() {
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbStack)
//                delay(300)
//                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
            } catch (e: Exception) {
                logger.error("stack fail in HomeViewModel/stack(): ${e.message}")
//                baseRepository.addNewErrorLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    errorContent = "dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}",
//                )
            }
        }
    }

    fun getCreateByteArrayDispenseBill(number: Int): ByteArray {
        require(number in 1..35) { "Input must be between 1 and 35" }

        val byte1 = 0x03.toByte()
        val byte2 = 0x01.toByte()
        val byte3 = 0x01.toByte()
        val byte4 = 0x00.toByte()
        val byte5 = 0x1C.toByte()
        val byte6 = number.toByte()

        // Calculate checksum (byte7) as per the given pattern
        val checksum = (0x1E - (number - 1)).toByte()

        return byteArrayOf(byte1, byte2, byte3, byte4, byte5, byte6, checksum)
    }


    fun dispensedAll() {
        viewModelScope.launch {
            try {
                _dispensedBill.value = true
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
//                _processCashBoxSuccess.value = false
                delay(300)
                logger.debug("numberRottenBoxBalance: ${_state.value.numberRottenBoxBalance}")
                if(_state.value.numberRottenBoxBalance <= 0) {
                    sendEvent(Event.Toast("Rotten box balance is 0"))
                } else {
                    portConnectionDatasource.sendCommandCashBox(getCreateByteArrayDispenseBill(_state.value.numberRottenBoxBalance))
                    delay(300)
//                    portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
                }
                _dispensedBill.value = false
            } catch (e: Exception) {
                logger.error("dispensed all fail in HomeViewModel/dispensedAll(): ${e.message}")
//                baseRepository.addNewErrorLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    errorContent = "dispensed one fail in HomeViewModel/dispensedOne(): ${e.message}",
//                )
            }
        }
    }

    fun resetCashBox() {
        portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOnLight)
    }

    fun offLight() {
        portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmTurnOffLight)
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        try {
            return hexString.split(",")
                .map { it.toInt(16).toByte() }
                .toByteArray()
        } catch (e: Exception) {
            throw e
        }
    }

    fun turnOnLight() {
        logger.debug("Turn on light")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(
                    ByteArrays().vmTurnOnLight,

                )
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun productDispense(
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
            0xAA.toByte(),
            0x55,
        )
        portConnectionDatasource.sendCommandVendingMachine(
            byteArray,

        )
    }

    fun setDrop(type: Int) {
        logger.debug("pollStatus")
        viewModelScope.launch {
            try {
                if(type == 2) {
                    portConnectionDatasource.sendCommandVendingMachine(ByteArrays().setDrop2)
                } else if(type == 3) {
                    portConnectionDatasource.sendCommandVendingMachine(ByteArrays().setDrop3)
                } else {
                    portConnectionDatasource.sendCommandVendingMachine(ByteArrays().setDrop5)
                }
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun turnOffLight() {
        logger.debug("Turn off light")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(
                    ByteArrays().vmTurnOffLight,

                )
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    fun drop1() {
        logger.debug("Turn off light")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(
                    ByteArrays().vmDrop1,

                )
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun drop11() {
        logger.debug("Turn off light")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(
                    ByteArrays().vmDrop11,

                )
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun drop111() {
        logger.debug("Turn off light")
        viewModelScope.launch {
            try {
                productDispenseNotSensor(0, 1)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }


    fun checkDropSensor() {
        logger.debug("check")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(
                    ByteArrays().vmCheckDropSensor,

                )
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getListMethodPayment() {
        logger.debug("getListMethodPayment")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val listPaymentMethod = settingsRepository.getListPaymentMethodFromServer()
                for (item in listPaymentMethod) {
                    logger.debug(item.toString())
                }
                baseRepository.writeDataToLocal(data = listPaymentMethod, path = pathFilePaymentMethod)
                _state.update { it.copy(
                    listPaymentMethod = listPaymentMethod,
                    isLoading = false,
                ) }
                sendEvent(Event.Toast("SUCCESS"))
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load list payment method fail in SetUpPaymentViewModel/getListMethodPayment(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
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

    fun downloadListMethodPayment() {
        logger.debug("downloadListMethodPayment")
        viewModelScope.launch {
            try {
                if(baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    val listPaymentMethod = settingsRepository.getListPaymentMethodFromServer()
                    if (!baseRepository.isFolderExists(pathFolderImagePayment)) {
                        baseRepository.createFolder(pathFolderImagePayment)
                    }
                    for (item in listPaymentMethod) {
                        if(item.imageUrl!!.isNotEmpty()) {
                            var notHaveError = true
                            for (i in 1..3) {
                                try {
                                    val request = ImageRequest.Builder(context = context)
                                        .data(item.imageUrl)
                                        .build()
                                    val result = withContext(Dispatchers.IO) {
                                        Coil.imageLoader(context).execute(request).drawable
                                    }
                                    if (result != null) {
                                        val file =
                                            File(pathFolderImagePayment, "${item.methodName}.png")
                                        withContext(Dispatchers.IO) {
                                            file.outputStream().use { outputStream ->
                                                result.toBitmap().compress(
                                                    Bitmap.CompressFormat.PNG,
                                                    1,
                                                    outputStream
                                                )
                                            }
                                        }
                                    }
                                    logger.debug("download ${item.imageUrl} success")
                                } catch (e: Exception) {
                                    notHaveError = false
                                    logger.debug("${e.message}")
                                } finally {
                                    if (notHaveError) break
                                }
                            }
                        }
                    }
                    baseRepository.addNewSetupLogToLocal(
                        machineCode = _state.value.initSetup!!.vendCode,
                        operationContent = "download list payment method success",
                        operationType = "setup payment",
                        username = _state.value.initSetup!!.username,
                    )
                    baseRepository.writeDataToLocal(data = listPaymentMethod, path = pathFilePaymentMethod)
                    sendEvent(Event.Toast("SUCCESS"))
                    _state.update { it.copy(
                        listPaymentMethod = listPaymentMethod,
                        isLoading = false,
                    ) }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load list payment method fail in SetUpPaymentViewModel/downloadListMethodPayment(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}