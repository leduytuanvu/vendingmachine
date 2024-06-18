package com.leduytuanvu.vendingmachine.features.settings.presentation.transaction.viewModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogSyncOrder
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFileSyncOrder
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.core.util.toId
import com.leduytuanvu.vendingmachine.features.settings.data.model.request.EndOfSessionRequest
import com.leduytuanvu.vendingmachine.features.settings.data.model.request.MoneyBoxRequest
import com.leduytuanvu.vendingmachine.features.settings.data.model.request.MoneyDataRequest
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.settings.presentation.transaction.viewState.TransactionViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class TransactionViewModel @Inject constructor (
    private val portConnectionDatasource: PortConnectionDatasource,
    private val baseRepository: BaseRepository,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger,
    private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(TransactionViewState())
    val state = _state.asStateFlow()

    private var cashBoxJob: Job? = null

    fun loadInitTransaction() {
        logger.debug("loadInitTransaction")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                logger.debug("${initSetup}")
                initSetup.timeStartSession = LocalDateTime.now().toString()
                logger.debug("1")
                if(initSetup.timeClosingSession.isEmpty()) {
                    initSetup.timeClosingSession = LocalDateTime.now().toString()
                }
                logger.debug("2")
                var listSyncOrder: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                    path = pathFileSyncOrder
                )
                if(listSyncOrder == null) {
                    listSyncOrder = arrayListOf()
                }
                logger.debug("3")
                var countTransactionByCash = 0
                var amountTransactionByCash = 0
                var countTransactionByOnline = 0
                var amountTransactionByOnline = 0

                if(listSyncOrder.isNotEmpty()) {
                    if(initSetup.timeClosingSession=="Never ended a session before") {
                        for(item in listSyncOrder) {
                            if(item.paymentMethodId=="cash") {
                                countTransactionByCash+=1
                                for(itemTmp in item.productDetails) {
                                    if(itemTmp.deliveryStatus == "success") {
                                        amountTransactionByCash+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                    }
                                }
                            } else {
                                countTransactionByOnline+=1
                                for(itemTmp in item.productDetails) {
                                    if(itemTmp.deliveryStatus == "success") {
                                        amountTransactionByOnline+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                    }
                                }
                            }
                        }
                    } else {
                        for(item in listSyncOrder) {
                            logger.debug("order time: ${item.orderTime}")
                            logger.debug("time close: ${initSetup.timeClosingSession}")
                            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
                            // Define the output date format with milliseconds
                            val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                            // Parse the date string to a ZonedDateTime object
                            val dateTime = ZonedDateTime.parse(item.orderTime, inputFormatter)
                            logger.debug("orderTimeConverter: ${dateTime.format(outputFormatter)}")
                            val comparisonResult = compareDateTimeStrings(dateTime.format(outputFormatter), initSetup.timeClosingSession)
                            if(comparisonResult>0) {
                                if(item.paymentMethodId=="cash") {
                                    countTransactionByCash+=1
                                    for(itemTmp in item.productDetails) {
                                        if(itemTmp.deliveryStatus == "success") {
                                            amountTransactionByCash+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                        }
                                    }
                                } else {
                                    countTransactionByOnline+=1
                                    for(itemTmp in item.productDetails) {
                                        if(itemTmp.deliveryStatus == "success") {
                                            amountTransactionByOnline+=(itemTmp.quantity!!*itemTmp.price!!.toInt())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Open port and start reading
                portConnectionDatasource.openPortCashBox(initSetup.portCashBox)
                if(!portConnectionDatasource.checkPortCashBoxStillStarting()) {
                    portConnectionDatasource.startReadingCashBox()
                }
                startCollectingData()
                // Get current cash
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbGetNumberRottenBoxBalance)
//                delay(5000)
                Logger.debug("okokokokk")
                _state.update { it.copy(
                    isLoading = false,
                    initSetup = initSetup,
                    listSyncOrder = listSyncOrder,
                    countTransactionByCash = countTransactionByCash,
                    amountTransactionByCash = amountTransactionByCash,
                    countTransactionByOnline = countTransactionByOnline,
                    amountTransactionByOnline = amountTransactionByOnline,
                ) }
            } catch (e: Exception) {
                Logger.debug("Exception: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showDialogConfirm(type: String) {
        viewModelScope.launch {
            if(_state.value.countTransactionByCash != 0 || _state.value.countTransactionByOnline != 0) {
                val sessionId = LocalDateTime.now().toId()
                _state.update { it.copy(
                    isConfirm = true,
                    typeConfirm = type,
                    sessionId = sessionId,
                ) }
            } else {
                sendEvent(Event.Toast("Not have any transaction!"))
            }
        }
    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update { it.copy(
                isConfirm = false,
                typeConfirm = "",
            ) }
        }
    }

    fun compareDateTimeStrings(dateString1: String, dateString2: String): Int {
        // Define the date format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        // Parse the date strings to LocalDateTime objects
        val dateTime1 = LocalDateTime.parse(dateString1, formatter)
        val dateTime2 = LocalDateTime.parse(dateString2, formatter)

        // Compare the LocalDateTime objects
        return dateTime1.compareTo(dateTime2)
    }

    fun getOldestDateInListSyncOrder(): String {
        // Use the ISO_INSTANT formatter which handles 'Z' correctly
        val formatter = DateTimeFormatter.ISO_INSTANT

        // Convert the list of date strings to ZonedDateTime objects
        val dateTimes = _state.value.listSyncOrder.map {
            Instant.parse(it.orderTime).atZone(ZoneId.of("UTC"))
        }

        // Find the oldest date
        val oldestDate = dateTimes.minOrNull()

        // Convert the oldest date back to the original string format
        return oldestDate?.format(formatter) ?: ""
    }

    fun convertDateTimeToLong(dateString: String): Long {
        // Define the date format
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        // Parse the date string to a ZonedDateTime object
        val dateTime = ZonedDateTime.parse(dateString, formatter.withZone(ZoneId.of("UTC")))

        // Convert the ZonedDateTime to an Instant
        val instant = dateTime.toInstant()

        // Convert the Instant to milliseconds since the Unix epoch
        return instant.toEpochMilli()
    }


    fun endOfSession(type: String) {
        viewModelScope.launch {
            if(_state.value.countTransactionByCash != 0 || _state.value.countTransactionByOnline != 0) {
                if(baseRepository.isHaveNetwork(context)) {
                    try {
                        _state.update { it.copy(isLoading = true) }
                        var countMomo = 0
                        var amountMomo = 0
                        var countVnPay = 0
                        var amountVnPay = 0
                        for(item in _state.value.listSyncOrder) {
                            if(item.paymentMethodId=="momo") {
                                countMomo++
                                for(tmpItem in item.productDetails) {
                                    if(tmpItem.deliveryStatus == "success") {
                                        amountMomo+=(tmpItem.quantity!!*tmpItem.price!!.toInt())
                                    }
                                }
                            }
                            if(item.paymentMethodId=="vnpay") {
                                countVnPay++
                                for(tmpItem in item.productDetails) {
                                    if(tmpItem.deliveryStatus == "success") {
                                        amountVnPay+=(tmpItem.quantity!!*tmpItem.price!!.toInt())
                                    }
                                }
                            }
                        }
                        logger.debug("count momo: ${countMomo}, amount momo: ${amountMomo}, count vnpay: ${countVnPay}, amount vnpay: ${amountVnPay}")
                        val listMoneyDataRequest: ArrayList<MoneyDataRequest> = arrayListOf()
                        val moneyDataRequestByCash = MoneyDataRequest(
                            paymentMethodId = "cash",
                            paymentAmount = _state.value.amountTransactionByCash,
                            orderQuantity = _state.value.countTransactionByCash,
                        )
                        listMoneyDataRequest.add(moneyDataRequestByCash)
                        val moneyDataRequestByMomo = MoneyDataRequest(
                            paymentMethodId = "momo",
                            paymentAmount = amountMomo,
                            orderQuantity = countMomo,
                        )
                        listMoneyDataRequest.add(moneyDataRequestByMomo)
                        val moneyDataRequestByVnPay = MoneyDataRequest(
                            paymentMethodId = "vnpay",
                            paymentAmount = amountVnPay,
                            orderQuantity = countVnPay,
                        )
                        listMoneyDataRequest.add(moneyDataRequestByVnPay)
                        val listMoneyBox: ArrayList<MoneyBoxRequest> = arrayListOf()
                        val moneyBoxRequest = MoneyBoxRequest(
                            denomination = 10000,
                            moneyQuantity = _state.value.numberRottenBoxBalance,
                        )
                        listMoneyBox.add(moneyBoxRequest)
                        val oldestDateTimeInListSyncOrder = getOldestDateInListSyncOrder()
                        val timeStartSession = if(_state.value.initSetup!!.timeStartSession.isNotEmpty()) {
                            _state.value.initSetup!!.timeStartSession
                        } else {
                            oldestDateTimeInListSyncOrder
                        }

                        val timeStartTmp = convertDateTimeToLong(timeStartSession)
                        logger.debug(timeStartTmp.toString())
                        val timeEndTmp = convertDateTimeToLong(LocalDateTime.now().toString())
                        logger.debug(timeEndTmp.toString())
                        val endOfSessionRequest = EndOfSessionRequest(
                            sessionId = _state.value.sessionId,
                            sessionType = type,
                            machineCode = _state.value.initSetup!!.vendCode,
                            androidId = _state.value.initSetup!!.androidId,
                            timeStart = timeStartTmp,
                            timeEnd = timeEndTmp,
                            moneyData = listMoneyDataRequest,
                            moneyBox = listMoneyBox,
                        )
                        val response = settingsRepository.endOfSession(endOfSessionRequest)
                        if(response.code==200) {
                            sendEvent(Event.Toast("SUCCESS"))
                            _state.value.initSetup!!.timeStartSession = LocalDateTime.now().toString()
                            _state.value.initSetup!!.timeClosingSession = LocalDateTime.now().toString()
                            val initSetup = _state.value.initSetup
                            baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)

                            _state.update { it.copy(
                                initSetup = initSetup,
                                isConfirm = false,
                                countTransactionByCash = 0,
                                amountTransactionByCash = 0,
                                countTransactionByOnline = 0,
                                amountTransactionByOnline = 0,
                            ) }
                        } else {
                            sendEvent(Event.Toast("FAIL"))
                        }
                    } catch (e: Exception) {
                        logger.debug("${e.message}")
                        baseRepository.addNewErrorLogToLocal(
                            machineCode = _state.value.initSetup!!.vendCode,
                            errorContent = "Error while end of monthly session: ${e.message}"
                        )
                    } finally {
                        _state.update { it.copy(isLoading = false) }
                    }
                } else {
                    sendEvent(Event.Toast("Not have internet, please try again!"))
                }
            } else {
                sendEvent(Event.Toast("Not have any transaction!"))
            }
        }
    }
//
//    fun endOfDailySession() {
//        viewModelScope.launch {
//            if(baseRepository.isHaveNetwork(context)) {
//                try {
//                    _state.update { it.copy(isLoading = true) }
//                    var countMomo = 0
//                    var amountMomo = 0
//                    var countVnPay = 0
//                    var amountVnPay = 0
//                    for(item in _state.value.listSyncOrder) {
//                        if(item.paymentMethodId=="momo") {
//                            countMomo++
//                            for(tmpItem in item.productDetails) {
//                                if(tmpItem.deliveryStatus == "success") {
//                                    amountMomo+=(tmpItem.quantity!!*tmpItem.price!!.toInt())
//                                }
//                            }
//                        }
//                        if(item.paymentMethodId=="vnpay") {
//                            countVnPay++
//                            for(tmpItem in item.productDetails) {
//                                if(tmpItem.deliveryStatus == "success") {
//                                    amountVnPay+=(tmpItem.quantity!!*tmpItem.price!!.toInt())
//                                }
//                            }
//                        }
//                    }
//                    logger.debug("count momo: ${countMomo}, amount momo: ${amountMomo}, count vnpay: ${countVnPay}, amount vnpay: ${amountVnPay}")
//                    val listMoneyDataRequest: ArrayList<MoneyDataRequest> = arrayListOf()
//                    val moneyDataRequestByCash = MoneyDataRequest(
//                        paymentMethodId = "cash",
//                        paymentAmount = _state.value.amountTransactionByCash,
//                        orderQuantity = _state.value.countTransactionByCash,
//                    )
//                    listMoneyDataRequest.add(moneyDataRequestByCash)
//                    val moneyDataRequestByMomo = MoneyDataRequest(
//                        paymentMethodId = "momo",
//                        paymentAmount = amountMomo,
//                        orderQuantity = countMomo,
//                    )
//                    listMoneyDataRequest.add(moneyDataRequestByMomo)
//                    val moneyDataRequestByVnPay = MoneyDataRequest(
//                        paymentMethodId = "vnpay",
//                        paymentAmount = amountVnPay,
//                        orderQuantity = countVnPay,
//                    )
//                    listMoneyDataRequest.add(moneyDataRequestByVnPay)
//                    val listMoneyBox: ArrayList<MoneyBoxRequest> = arrayListOf()
//                    val moneyBoxRequest = MoneyBoxRequest(
//                        denomination = 10000,
//                        moneyQuantity = _state.value.numberRottenBoxBalance,
//                    )
//                    listMoneyBox.add(moneyBoxRequest)
//                    val oldestDateTimeInListSyncOrder = getOldestDateInListSyncOrder()
//                    val timeStartSession = if(_state.value.initSetup!!.timeStartSession.isNotEmpty()) {
//                        _state.value.initSetup!!.timeStartSession
//                    } else {
//                        oldestDateTimeInListSyncOrder
//                    }
//                    val endOfSessionRequest = EndOfSessionRequest(
//                        sessionId = LocalDateTime.now().toId(),
//                        sessionType = "daily",
//                        machineCode = _state.value.initSetup!!.vendCode,
//                        androidId = _state.value.initSetup!!.androidId,
//                        timeStart = convertDateTimeToLong(timeStartSession),
//                        timeEnd = convertDateTimeToLong(LocalDateTime.now().toString()),
//                        moneyData = listMoneyDataRequest,
//                        moneyBox = listMoneyBox,
//                    )
//                    val response = settingsRepository.endOfSession(endOfSessionRequest)
//                    if(response.code==200) {
//                        sendEvent(Event.Toast("SUCCESS"))
//                        _state.value.initSetup!!.timeStartSession = LocalDateTime.now().toString()
//                        _state.value.initSetup!!.timeClosingSession = LocalDateTime.now().toString()
//                        val initSetup = _state.value.initSetup
//                        baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
//                        _state.update { it.copy(initSetup = initSetup) }
//                    } else {
//                        sendEvent(Event.Toast("FAIL"))
//                    }
//                } catch (e: Exception) {
//                    baseRepository.addNewErrorLogToLocal(
//                        machineCode = _state.value.initSetup!!.vendCode,
//                        errorContent = "Error while end of daily session: ${e.message}"
//                    )
//                } finally {
//                    _state.update { it.copy(isLoading = false) }
//                }
//            } else {
//                sendEvent(Event.Toast("Not have internet, please try again!"))
//            }
//        }
//    }

    fun processingDataFromCashBox(dataByteArray: ByteArray) {
        try {
            val dataHexString = dataByteArray.joinToString(",") { "%02X".format(it) }
            logger.debug(dataHexString)
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
                _state.update { it.copy(numberRottenBoxBalance = numberRottenBoxBalance) }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun startCollectingData() {
        cashBoxJob = viewModelScope.launch {
            portConnectionDatasource.dataFromCashBox.collect { data ->
                processingDataFromCashBox(data)
            }
        }
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
    fun closePort() {
        cashBoxJob?.cancel()
        cashBoxJob = null
        portConnectionDatasource.closeCashBoxPort()
    }
}