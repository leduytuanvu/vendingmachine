package com.combros.vendingmachine.features.home.presentation.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.model.LogServer
import com.combros.vendingmachine.common.base.domain.model.LogsLocal
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository
import com.combros.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.combros.vendingmachine.features.home.domain.repository.HomeRepository
import com.combros.vendingmachine.features.home.presentation.viewState.HomeViewState
import com.combros.vendingmachine.features.settings.data.model.response.PaymentMethodResponse
import com.combros.vendingmachine.features.settings.domain.model.Slot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.BatteryManager

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.combros.vendingmachine.common.base.domain.model.LogDepositWithdraw
import com.combros.vendingmachine.features.home.data.model.request.CheckPaymentResultOnlineRequest
import com.combros.vendingmachine.features.home.data.model.request.DepositAndWithdrawMoneyRequest
import com.combros.vendingmachine.features.home.data.model.request.GetQrCodeRequest
import com.combros.vendingmachine.features.home.data.model.request.ProductDetailRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdateDeliveryStatusRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdatePromotionRequest
import com.combros.vendingmachine.features.home.domain.model.CartExtra
import com.combros.vendingmachine.features.home.domain.model.Extra
import org.threeten.bp.LocalDateTime
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

import javax.inject.Inject
import com.combros.vendingmachine.common.base.domain.model.LogSyncOrder
import com.combros.vendingmachine.common.base.domain.model.LogUpdateDeliveryStatus
import com.combros.vendingmachine.common.base.domain.model.LogUpdateInventory
import com.combros.vendingmachine.common.base.domain.model.LogUpdatePromotion

import com.combros.vendingmachine.core.util.*

import com.combros.vendingmachine.features.home.data.model.request.ProductSyncOrderRequest
import com.combros.vendingmachine.features.home.data.model.request.DataSyncOrderRequest
import com.combros.vendingmachine.features.home.data.model.request.SyncOrderRequest
import com.combros.vendingmachine.features.home.data.model.request.UpdateInventoryRequest
import com.combros.vendingmachine.R

enum class DropSensorResult(val data: String) {
    INITIALIZATION("INITIALIZATION"),
    ANOTHER("ANOTHER"),
    UNKNOWN_ERROR("UNKNOWN_ERROR"),
    ERROR_00_5C_50_00_AC_PRODUCT_NOT_FALL("00,5C,50,00,AC"),
    ERROR_00_5C_50_AA_56_PRODUCT_FALL("00,5C,50,AA,56"),
    SUCCESS("00,5D,00,AA,07"),
    ROTATED_BUT_PRODUCT_NOT_FALL("00,5D,00,00,5D"),
    NOT_ROTATED("00,5C,40,00,9C"),
    NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM("00,5C,02,00,5E"),
    ROTATED_BUT_INSUFFICIENT_ROTATION("00,5D,00,CC,29"),
    ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED("00,5D,00,33,90"),
    SENSOR_HAS_AN_OBSTACLE("00,5C,03,00,5F"),
}

data class BatteryStatus(val level: Int, val isCharging: Boolean)

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class HomeViewModel @Inject constructor (
    private val homeRepository: HomeRepository,
    private val baseRepository: BaseRepository,
    private val portConnectionDatasource: PortConnectionDatasource,
    private val byteArrays: ByteArrays,
    private val context: Context,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeViewState())
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    private var countdownTimer: CountDownTimer? = null
    private var countdownTimerCallApi: CountDownTimer? = null

    private var debounceDelay = 100L
    private var debounceJob: Job? = null

    private var cashBoxJob: Job? = null
    private var vendingMachineJob: Job? = null

    private val _setupCashBox = MutableStateFlow(false)
    val setupCashBox: StateFlow<Boolean> = _setupCashBox.asStateFlow()

    private val _dataTmpCashBox = MutableStateFlow("")
    val dataTmpCashBox: StateFlow<String> = _dataTmpCashBox.asStateFlow()

    private val _dataTmpVendingMachine = MutableStateFlow("")
    val dataTmpVendingMachine: StateFlow<String> = _dataTmpVendingMachine.asStateFlow()

    private val _isCashBoxNormal = MutableStateFlow(false)
    val isCashBoxNormal: StateFlow<Boolean> = _isCashBoxNormal.asStateFlow()

    private val _numberRottenBoxBalance = MutableStateFlow(-1)
    val numberRottenBoxBalance: StateFlow<Int> = _numberRottenBoxBalance.asStateFlow()

    private val _statusDropProduct = MutableStateFlow(DropSensorResult.ANOTHER)
    val statusDropProduct: StateFlow<DropSensorResult> = _statusDropProduct.asStateFlow()

//    fun initLoad() {
//        logger.debug("initLoad")
//        viewModelScope.launch {
//            try {
//                logger.debug("0")
//                portConnectionDatasource.openPortCashBox(_state.value.initSetup!!.portCashBox)
//                if(!portConnectionDatasource.checkPortCashBoxStillStarting()) {
//                    portConnectionDatasource.startReadingCashBox()
//                }
//                portConnectionDatasource.openPortVendingMachine(_state.value.initSetup!!.portVendingMachine)
//                portConnectionDatasource.startReadingVendingMachine()
//                logger.debug("1")
//                _setupCashBox.value = false
//                sendCommandCashBox(byteArrays.cbEnableType3456789)
//                logger.debug("2")
//                delay(260)
//                if(_setupCashBox.value) {
//                    logger.debug("set cbEnableType3456789 success")
//                } else {
//                    logger.debug("set cbEnableType3456789 fail")
//                }
//                logger.debug("3")
//                _setupCashBox.value = false
//                sendCommandCashBox(byteArrays.cbSetRecyclingBillType4)
//                delay(260)
//                if(_setupCashBox.value) {
//                    logger.debug("set cbSetRecyclingBillType4 success")
//                } else {
//                    logger.debug("set cbSetRecyclingBillType4 fail")
//                }
//                sendCommandVendingMachine(byteArrays.vmReadTemp)
//                logger.debug("4")
//            } catch (e: Exception) {
//                sendEvent(Event.Toast("${e.message}"))
//            } finally {
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }

//    fun checkDriverBoardsAndCargoLanes() {
//        viewModelScope.launch {
//            val totalDriverBoards = 10
//            var totalCargoLanes = 6
//            for(indexCargoLane in 1 ..  totalCargoLanes) {
//                for(indexDriverBoard in 0 until  totalDriverBoards) {
//                    logger.debug("indexDriverBoard: $indexDriverBoard, indexCargoLane: ${indexCargoLane}")
//                    val command = createCommandCheckDriverBoardsAndCargoLanes(indexDriverBoard, indexCargoLane)
//                    sendCommandVendingMachine(command)
//                    delay(2000)
//                }
//            }
//        }
//    }

    fun createCommandCheckDriverBoardsAndCargoLanes(driverBoard: Int, cargoLane: Int): ByteArray {
        val firstByte = driverBoard.toByte()
        val secondByte = (0xFF - driverBoard).toByte()
        val thirdByte = (0x78 + cargoLane).toByte()
        val fourthByte = (0x86 - driverBoard).toByte()
        val fifthByte = 0x55.toByte()
        val sixthByte = 0xAA.toByte()
        return byteArrayOf(firstByte, secondByte, thirdByte, fourthByte, fifthByte, sixthByte)
    }

//    fun sendCommandAndGetResponse(command: ByteArray): ByteArray {
//        // Simulate sending command and getting response
//        // Replace this with actual communication code
//        return when (command[2]) {
//            0.toByte() -> byteArrayOf(0x00, 0xFF.toByte(), 0x79.toByte(), 0x86.toByte(), 0x55.toByte(), 0xAA.toByte())
//            else -> byteArrayOf(0x01, 0xFE.toByte(), 0x7A.toByte(), 0x85.toByte(), 0x55.toByte(), 0xAA.toByte())
//        }
//    }

//    fun analyzeResponse(response: ByteArray): Boolean {
//        // Analyze the response based on the provided examples
//        return response[2] == 0x79.toByte() || response[2] == 0x7A.toByte()
//    }

    fun sendCommandCashBox(byteArray: ByteArray) {
        logger.debug("send cash box: ${byteArrayToHexString(byteArray)}")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(byteArray)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun getTemp() {
        logger.debug("getTemp")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(ByteArrays().vmReadTemp)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun getTypeNetworkAndBatteryStatus() {
//        logger.debug("getTemp")
//        viewModelScope.launch {
//            try {
//                val networkType = getNetworkType(context)
////                val batteryStatus = getBatteryStatus(context)
//                logger.debug("+++++++++ networkType: $networkType")
////                logger.debug("+++++++++ batteryStatus: $batteryStatus")
//            } catch (e: Exception) {
//                sendEvent(Event.Toast("${e.message}"))
//            } finally {
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }

    fun sendCommandVendingMachine(byteArray: ByteArray) {
//        logger.debug("send vending machine: ${byteArrayToHexString(byteArray)}")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandVendingMachine(byteArray)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun closePort() {
        cashBoxJob?.cancel()
        vendingMachineJob?.cancel()
        cashBoxJob = null
        vendingMachineJob = null
        portConnectionDatasource.closeVendingMachinePort()
        portConnectionDatasource.closeCashBoxPort()
    }

    private fun observePortData() {
        cashBoxJob = viewModelScope.launch {
            portConnectionDatasource.dataFromCashBox.collect { data ->
                processDataFromCashBox(data)
            }
        }

        vendingMachineJob = viewModelScope.launch {
            portConnectionDatasource.dataFromVendingMachine.collect { data ->
                processDataFromVendingMachine(data)
            }
        }
    }

//    private fun processDataFromVendingMachine(dataByteArray: ByteArray) {
//        try {
//            val dataHexString = byteArrayToHexString(dataByteArray)
////            Logger.info("-------> data from vending machine: $dataHexString")
//            when(dataHexString) {
//                "00,5D,00,00,5D" -> {
//                    _statusDropProduct.value = DropSensorResult.ROTATED_BUT_PRODUCT_NOT_FALL
//                }
//                "00,5D,00,AA,07" -> {
//                    _statusDropProduct.value = DropSensorResult.SUCCESS
//                }
//                "00,5C,40,00,9C" -> {
//                    _statusDropProduct.value = DropSensorResult.NOT_ROTATED
//                }
//                "00,5C,02,00,5E" -> {
//                    _statusDropProduct.value = DropSensorResult.NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM
//                }
//                "00,5D,00,CC,29" -> {
//                    _statusDropProduct.value = DropSensorResult.ROTATED_BUT_INSUFFICIENT_ROTATION
//                }
//                "00,5D,00,33,90" -> {
//                    _statusDropProduct.value = DropSensorResult.ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED
//                }
//                "00,5C,03,00,5F" -> {
//                    _statusDropProduct.value = DropSensorResult.SENSOR_HAS_AN_OBSTACLE
//                }
//            }
//            if(dataByteArray.size==5) {
//                if(dataByteArray[0] == 0x00.toByte()
//                    && dataByteArray[1] == 0x5D.toByte()
//                    && (dataByteArray[4] == 0x66.toByte()
//                            || dataByteArray[4] == 0x65.toByte()
//                            || dataByteArray[4] == 0x6B.toByte()
//                            || dataByteArray[4] == 0x67.toByte()
//                            || dataByteArray[4] == 0x63.toByte()
//                            || dataByteArray[4] == 0x64.toByte())
//                ) {
//                    logger.debug("============================== data: $dataHexString")
//                    if(dataByteArray[2] == 0xEB.toByte()) {
//                        _state.update { it.copy(temp1 = "không thể kết nối") }
//                    } else {
//                        _state.update { it.copy(temp1 = "${dataByteArray[2].toInt()}") }
//                    }
//                    if(dataByteArray[3] == 0xEB.toByte()) {
//                        _state.update { it.copy(temp2 = "không thể kết nối") }
//                    } else {
//                        _state.update { it.copy(temp2 = "${dataByteArray[3].toInt()}") }
//                    }
//                }
//            }
//        } catch (e: Exception) {
//            Logger.error("Error processing cash box data: ${e.message}", e)
//        }
//    }

    private suspend fun processDataFromVendingMachine(dataByteArray: ByteArray) {
        try {
            val dataHexString = byteArrayToHexString(dataByteArray)
//            logger.debug("============================== data from vending machine: $dataHexString")
            if(dataHexString.isNotEmpty()) {
                if(_state.value.isVendingMachineBusy) {
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
                        else -> DropSensorResult.ANOTHER
                    }

                    if (result != DropSensorResult.ANOTHER) {
                        _statusDropProduct.tryEmit(result)
                        logger.debug("Result emitted to dispenseResults: $result")
                    }
                } else {
//                    if(dataByteArray[0] == 0x00.toByte()
//                        && dataByteArray[1] == 0x5D.toByte()
//                        && (dataByteArray[4] == 0x66.toByte()
//                                || dataByteArray[4] == 0x65.toByte()
//                                || dataByteArray[4] == 0x6B.toByte()
//                                || dataByteArray[4] == 0x67.toByte()
//                                || dataByteArray[4] == 0x68.toByte()
//                                || dataByteArray[4] == 0x69.toByte()
//                                || dataByteArray[4] == 0x63.toByte()
//                                || dataByteArray[4] == 0x64.toByte())
                    if(_dataTmpVendingMachine.value != dataHexString) {
                        _dataTmpVendingMachine.value = dataHexString
                        if(dataByteArray[0] == 0x00.toByte()
                            && dataByteArray[1] == 0x5D.toByte()
                            && (dataByteArray[4] != 0x5D.toByte())
                            && (dataByteArray[4] != 0x5E.toByte())
                        ) {
                            if(dataByteArray[2] == 0xEB.toByte() && dataByteArray[3] == 0xEB.toByte()) {
                                _state.update { it.copy(temp1 = "không thể kết nối") }
                                baseRepository.addNewTemperatureLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    cabinetCode = "MT01",
                                    currentTemperature = "không thể kết nối",
                                )
                            } else {
                                if(dataByteArray[2] != 0xEB.toByte()){
                                    _state.update { it.copy(temp1 = "${dataByteArray[2].toInt()}") }
                                    baseRepository.addNewTemperatureLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        cabinetCode = "MT01",
                                        currentTemperature = "${dataByteArray[2].toInt()}",
                                    )
                                } else {
                                    _state.update { it.copy(temp1 = "${dataByteArray[3].toInt()}") }
                                    baseRepository.addNewTemperatureLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        cabinetCode = "MT01",
                                        currentTemperature = "${dataByteArray[3].toInt()}",
                                    )
                                }

                            }
//                            if(dataByteArray[3] == 0xEB.toByte()) {
//                                _state.update { it.copy(temp2 = "không thể kết nối") }
//                                baseRepository.addNewTemperatureLogToLocal(
//                                    machineCode = _state.value.initSetup!!.vendCode,
//                                    cabinetCode = "MT01",
//                                    currentTemperature = "temp 2 không thể kết nối",
//                                )
//                            } else {
//                                _state.update { it.copy(temp2 = "${dataByteArray[3].toInt()}") }
//                                baseRepository.addNewTemperatureLogToLocal(
//                                    machineCode = _state.value.initSetup!!.vendCode,
//                                    cabinetCode = "MT01",
//                                    currentTemperature = "${dataByteArray[3].toInt()}",
//                                )
//                            }
                        }
                        else {
                            if(dataByteArray[4] == 0x5D.toByte() || dataByteArray[4] == 0x5E.toByte()) {
                                if(dataByteArray[4] == 0x5D.toByte()) {
                                    baseRepository.addNewDoorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        cabinetCode = "MT01",
                                        operationType = "door open",
                                    )
                                    Logger.info("door open")
                                } else {
                                    baseRepository.addNewDoorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        cabinetCode = "MT01",
                                        operationType = "door close",
                                    )
                                    Logger.info("door close")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.error("Error processing vending machine data: ${e.message}", e)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun getNetworkType(context: Context): String {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork = connectivityManager.activeNetwork ?: return "No Connection"
//        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return "No Connection"
//
//        return when {
//            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
//            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
//                when (networkCapabilities.networkSpecifier?.toString()!!.lowercase()) {
//                    "lte" -> "4G"
//                    "nr" -> "5G"
//                    else -> "Mobile Data"
//                }
//            }
//            else -> "Unknown"
//        }
//    }

//    fun getBatteryStatus(context: Context): BatteryStatus {
//        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
//        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
//
//        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
//        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
//        val batteryPct = (level / scale.toFloat() * 100).toInt()
//
//        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
//        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
//
//        return BatteryStatus(batteryPct, isCharging)
//    }

    private fun checkDropSensor(numberBoard: Int = 0) {
        val byteArrayNumberBoard: Byte = numberBoard.toByte()
        val byteArray: ByteArray =
            byteArrayOf(
                byteArrayNumberBoard,
                (0xFF - numberBoard).toByte(),
                0x64,
                0x9B.toByte(),
                0xAA.toByte(),
                0x55,
            )
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }


    fun dropProduct() {
        logger.info("dropProduct")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isVendingMachineBusy = true) }

                // Stop countdown timer
                if(countdownTimer!=null) {
                    countdownTimer!!.cancel()
                    countdownTimer = null
                }
                if(countdownTimerCallApi!=null) {
                    countdownTimerCallApi!!.cancel()
                    countdownTimerCallApi = null
                }

                // List slot drop fail
                val listSlotDropFail: ArrayList<Slot> = arrayListOf()
                // List all slot drop fail
                val listAllSlotDropFail: ArrayList<Slot> = arrayListOf()
                // List slot drop success
                val listSlotDropSuccess: ArrayList<Slot> = arrayListOf()
                // List slot not found
                val listSlotNotFound: ArrayList<Slot> = arrayListOf()
                // List slot show in home
                val listSlotShowInHome: ArrayList<Slot> = arrayListOf()

                // List sync order transaction
                var listLogSyncOrderTransaction: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                    path = pathFileSyncOrderTransaction
                )
                if(listLogSyncOrderTransaction.isNullOrEmpty()) {
                    listLogSyncOrderTransaction = arrayListOf()
                }

                // List slot in cart
                val listSlotInCart = _state.value.listSlotInCard

                // The sensor has an obstruction
                var sensorHasAnObstruction = false
                // Quantity product need drop
                var quantityNeedDrop = 0
                // Quantity product dropped
                var quantityDropped = 0
                // Cash dropped
                var cashDropped = 0

                // Get quantity product need drop
                for (item in listSlotInCart) {
                    quantityNeedDrop += item.inventory
                }

                outerLoop@ for (item in listSlotInCart) {
                    for (index in 1..item.inventory) {
                        _statusDropProduct.value = DropSensorResult.INITIALIZATION
                        checkDropSensor(0)
                        Log.d("debugdropproduct", "1")
                        var resultCheckDropSensor = withTimeoutOrNull(5000) {
                            statusDropProduct.first { it != DropSensorResult.INITIALIZATION }
                        }
                        Log.d("debugdropproduct", "2")
                        if(resultCheckDropSensor == null) {
                            Log.d("debugdropproduct", "TIMEOUT_WAITING_FOR_DISPENSE_RESULT")
                            var indexCheck = listAllSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "TIMEOUT_WAITING_FOR_CHECK_DROP_SENSOR" }
                            if(indexCheck!=-1) {
                                listAllSlotDropFail[indexCheck].inventory++
                            } else {
                                val tmpItem = item
                                tmpItem.inventory = 1
                                tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                listAllSlotDropFail.add(tmpItem)
                            }
                            indexCheck = listSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM" }
                            if(indexCheck!=-1) {
                                listSlotDropFail[indexCheck].inventory++
                            } else {
                                val tmpItem = item
                                tmpItem.inventory = 1
                                tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                listSlotDropFail.add(tmpItem)
                            }
                        } else {
                            Log.d("debugdropproduct", "resultCheckDropSensor: $resultCheckDropSensor")
                            if(_state.value.initSetup!!.dropSensor=="OFF") {
                                resultCheckDropSensor = DropSensorResult.ANOTHER
                            }
                            if(resultCheckDropSensor == DropSensorResult.NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM) {
                                var indexCheck = listAllSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM" }
                                if(indexCheck!=-1) {
                                    listAllSlotDropFail[indexCheck].inventory++
                                } else {
                                    val tmpItem = item
                                    tmpItem.inventory = 1
                                    tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                    listAllSlotDropFail.add(tmpItem)
                                }
                                indexCheck = listSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM" }
                                if(indexCheck!=-1) {
                                    listSlotDropFail[indexCheck].inventory++
                                } else {
                                    val tmpItem = item
                                    tmpItem.inventory = 1
                                    tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                    listSlotDropFail.add(tmpItem)
                                }
                            } else {
                                var messDropFailed = ""
                                var checkDropProductFailAll = true
                                val slot = homeRepository.getSlotDrop(item.productCode)
                                if (slot != null) {
                                    _statusDropProduct.value = DropSensorResult.ANOTHER
                                    if(_state.value.initSetup!!.dropSensor=="OFF") {
                                        productDispenseNotSensor(0, slot.slot)
                                    } else {
                                        productDispense(0, slot.slot)
                                    }
                                    var result = withTimeoutOrNull(18000L) {
                                        statusDropProduct.first { it != DropSensorResult.ANOTHER }
                                    }
                                    if (result == null) {
                                        messDropFailed = "TIMEOUT_WAITING_FOR_DISPENSE_RESULT"
                                        logger.debug(messDropFailed)
                                        homeRepository.lockSlot(slot.slot)
                                        val indexAllSlotDropFail = listAllSlotDropFail.indexOfFirst { it.slot == slot.slot }
                                        if(indexAllSlotDropFail!=-1) {
                                            listAllSlotDropFail[indexAllSlotDropFail].messDrop = messDropFailed
                                            listAllSlotDropFail[indexAllSlotDropFail].inventory++
                                        } else {
                                            slot.inventory = 1
                                            slot.messDrop = messDropFailed
                                            listAllSlotDropFail.add(slot)
                                        }
                                        continue
                                    }
                                    if(_state.value.initSetup!!.dropSensor=="OFF"
                                        && (result == DropSensorResult.ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED
                                                || result == DropSensorResult.SENSOR_HAS_AN_OBSTACLE
                                                || result == DropSensorResult.ROTATED_BUT_PRODUCT_NOT_FALL
                                                || result == DropSensorResult.ROTATED_BUT_INSUFFICIENT_ROTATION)
                                    ) {
                                        result = DropSensorResult.SUCCESS
                                    }
                                    when (result) {
                                        DropSensorResult.SUCCESS -> {
                                            val indexSlotDropSuccess = listSlotDropSuccess.indexOfFirst { it.slot == slot.slot }
                                            if(indexSlotDropSuccess!=-1) {
                                                listSlotDropSuccess[indexSlotDropSuccess].messDrop = "SUCCESS"
                                                listSlotDropSuccess[indexSlotDropSuccess].inventory++
                                            } else {
                                                slot.inventory = 1
                                                slot.messDrop = "SUCCESS"
                                                listSlotDropSuccess.add(slot)
                                            }
                                            homeRepository.minusInventory(slot.slot)
                                            item.inventory--
                                            cashDropped += item.price
                                            quantityDropped++
                                            checkDropProductFailAll = false
                                        }
                                        DropSensorResult.ERROR_00_5C_50_AA_56_PRODUCT_FALL -> {
                                            val indexSlotDropSuccess = listSlotDropSuccess.indexOfFirst { it.slot == slot.slot }
                                            if(indexSlotDropSuccess!=-1) {
                                                listSlotDropSuccess[indexSlotDropSuccess].messDrop = "ERROR_00_5C_50_AA_56_PRODUCT_FALL"
                                                listSlotDropSuccess[indexSlotDropSuccess].inventory++
                                            } else {
                                                slot.inventory = 1
                                                slot.messDrop = "ERROR_00_5C_50_AA_56_PRODUCT_FALL"
                                                listSlotDropSuccess.add(slot)
                                            }
                                            homeRepository.minusInventory(slot.slot)
                                            item.inventory--
                                            cashDropped += item.price
                                            quantityDropped++
                                            checkDropProductFailAll = false
                                        }
                                        DropSensorResult.SENSOR_HAS_AN_OBSTACLE -> {
                                            messDropFailed = "SENSOR_HAS_AN_OBSTACLE"
                                            logger.debug("+++++++++++ $result")
                                            sensorHasAnObstruction = true
                                            var indexCheck = listAllSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "SENSOR_HAS_AN_OBSTACLE" }
                                            if (indexCheck!=-1) {
                                                listAllSlotDropFail[indexCheck].inventory++
                                            } else {
                                                val tmpItem = item
                                                tmpItem.inventory = 1
                                                tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                                listAllSlotDropFail.add(tmpItem)
                                            }
                                            indexCheck = listSlotDropFail.indexOfFirst { it.slot == item.slot && it.messDrop != "SENSOR_HAS_AN_OBSTACLE" }
                                            if (indexCheck!=-1) {
                                                listSlotDropFail[indexCheck].inventory++
                                            } else {
                                                val tmpItem = item
                                                tmpItem.inventory = 1
                                                tmpItem.messDrop = "NOT_ROTATED_AND_DROP_SENSOR_HAVE_PROBLEM"
                                                listSlotDropFail.add(tmpItem)
                                            }
//                                        break@outerLoop
                                        }
                                        else -> {
                                            messDropFailed = result.name
                                            val indexAllSlotDropFail = listAllSlotDropFail.indexOfFirst { it.slot == slot.slot }
                                            if(indexAllSlotDropFail!=-1) {
                                                listAllSlotDropFail[indexAllSlotDropFail].messDrop = messDropFailed
                                                listAllSlotDropFail[indexAllSlotDropFail].inventory++
                                            } else {
                                                slot.inventory = 1
                                                slot.messDrop = messDropFailed
                                                listAllSlotDropFail.add(slot)
                                            }
                                            homeRepository.lockSlot(slot.slot)
                                            val listAnotherSlot = homeRepository.getListAnotherSlot(item.productCode)
                                            if(listAnotherSlot.isEmpty()) {
                                                var countItemSlotDropSuccess = 0
                                                var countItemSlotDropFail = 0
                                                for(itemTmpCheckSlot in listSlotDropSuccess) {
                                                    if(itemTmpCheckSlot.productCode == slot.productCode){
                                                        countItemSlotDropSuccess += itemTmpCheckSlot.inventory
                                                    }
                                                }
                                                for(itemTmpCheckSlot in listAllSlotDropFail) {
                                                    if(itemTmpCheckSlot.productCode == slot.productCode){
                                                        countItemSlotDropFail += itemTmpCheckSlot.inventory
                                                    }
                                                }
                                                val indexAllAnotherSlotDropFail = listAllSlotDropFail.indexOfFirst { it.productCode == slot.productCode }
                                                listAllSlotDropFail[indexAllAnotherSlotDropFail].inventory = item.inventory
                                            } else {
                                                for (itemAnother in listAnotherSlot) {
                                                    val slotAnother = homeRepository.getSlotDrop(item.productCode)
                                                    logger.debug("slotAnother found: $slotAnother")
                                                    if (slotAnother != null) {
                                                        _statusDropProduct.value = DropSensorResult.ANOTHER
                                                        if(_state.value.initSetup!!.dropSensor=="OFF") {
                                                            productDispenseNotSensor(0, slotAnother.slot)
                                                        } else {
                                                            productDispense(0, slotAnother.slot)
                                                        }
                                                        var anotherResult = withTimeoutOrNull(18000L) {
                                                            statusDropProduct.first { it != DropSensorResult.ANOTHER }
                                                        }
                                                        if (anotherResult == null) {
                                                            logger.debug("Timeout waiting for another dispense result")
                                                            val indexAnotherAllSlotDropFail = listAllSlotDropFail.indexOfFirst { it.slot == slot.slot }
                                                            if(indexAnotherAllSlotDropFail!=-1) {
                                                                listAllSlotDropFail[indexAnotherAllSlotDropFail].messDrop = messDropFailed
                                                                listAllSlotDropFail[indexAnotherAllSlotDropFail].inventory++
                                                            } else {
                                                                slot.inventory = 1
                                                                slot.messDrop = messDropFailed
                                                                listAllSlotDropFail.add(slot)
                                                            }
                                                            homeRepository.lockSlot(slotAnother.slot)
                                                            continue
                                                        }
                                                        if(_state.value.initSetup!!.dropSensor=="OFF"
                                                            && (anotherResult == DropSensorResult.ROTATED_BUT_NO_SHORTAGES_OR_VIBRATIONS_WERE_DETECTED
                                                                    || anotherResult == DropSensorResult.SENSOR_HAS_AN_OBSTACLE
                                                                    || anotherResult == DropSensorResult.ROTATED_BUT_PRODUCT_NOT_FALL
                                                                    || anotherResult == DropSensorResult.ROTATED_BUT_INSUFFICIENT_ROTATION)
                                                        ) {
                                                            anotherResult = DropSensorResult.SUCCESS
                                                        }
                                                        when (anotherResult) {
                                                            DropSensorResult.SUCCESS -> {
                                                                val indexSlotDropSuccess = listSlotDropSuccess.indexOfFirst { it.slot == slotAnother.slot }
                                                                if(indexSlotDropSuccess!=-1) {
                                                                    listSlotDropSuccess[indexSlotDropSuccess].messDrop = "SUCCESS"
                                                                    listSlotDropSuccess[indexSlotDropSuccess].inventory++
                                                                } else {
                                                                    slotAnother.inventory = 1
                                                                    slotAnother.messDrop = "SUCCESS"
                                                                    listSlotDropSuccess.add(slotAnother)
                                                                }
                                                                homeRepository.minusInventory(slotAnother.slot)
                                                                item.inventory--
                                                                cashDropped += item.price
                                                                quantityDropped++
                                                                checkDropProductFailAll = false
                                                                break
                                                            }
                                                            DropSensorResult.ERROR_00_5C_50_AA_56_PRODUCT_FALL -> {
                                                                val indexSlotDropSuccess = listSlotDropSuccess.indexOfFirst { it.slot == slotAnother.slot }
                                                                if(indexSlotDropSuccess!=-1) {
                                                                    listSlotDropSuccess[indexSlotDropSuccess].messDrop = "ERROR_00_5C_50_AA_56_PRODUCT_FALL"
                                                                    listSlotDropSuccess[indexSlotDropSuccess].inventory++
                                                                } else {
                                                                    slotAnother.inventory = 1
                                                                    slotAnother.messDrop = "ERROR_00_5C_50_AA_56_PRODUCT_FALL"
                                                                    listSlotDropSuccess.add(slotAnother)
                                                                }
                                                                homeRepository.minusInventory(slotAnother.slot)
                                                                item.inventory--
                                                                cashDropped += item.price
                                                                quantityDropped++
                                                                checkDropProductFailAll = false
                                                                break
                                                            }
                                                            DropSensorResult.SENSOR_HAS_AN_OBSTACLE -> {
                                                                logger.debug("+++++SENSOR_HAS_AN_OBSTACLE: $result")
                                                                sensorHasAnObstruction = true
                                                                break@outerLoop
                                                            }
                                                            else -> {
                                                                messDropFailed = anotherResult.name
                                                                val indexAllAnotherSlotDropFail = listAllSlotDropFail.indexOfFirst { it.slot == slotAnother.slot }
                                                                if(indexAllAnotherSlotDropFail!=-1) {
                                                                    listAllSlotDropFail[indexAllAnotherSlotDropFail].messDrop = messDropFailed
                                                                    listAllSlotDropFail[indexAllAnotherSlotDropFail].inventory++
                                                                } else {
                                                                    slot.inventory = 1
                                                                    slot.messDrop = messDropFailed
                                                                    listAllSlotDropFail.add(slot)
                                                                }
                                                                homeRepository.lockSlot(slotAnother.slot)
                                                            }
                                                        }
                                                    } else {
                                                        listSlotNotFound.add(item)
                                                        val indexAnotherAllSlotDropFail = listAllSlotDropFail.indexOfFirst { it.productCode == item.productCode }
                                                        if(indexAnotherAllSlotDropFail!=-1) {
                                                            listAllSlotDropFail[indexAnotherAllSlotDropFail].inventory++
                                                        } else {
                                                            item.slot = -1
                                                            item.inventory = 1
                                                            item.messDrop = "NOT FOUND ANY SLOT HAVE PRODUCT CODE IS ${item.productCode}"
                                                            listAllSlotDropFail.add(item)
                                                        }

                                                        val indexAllSlotDropFailCheck = listSlotDropFail.indexOfFirst { it.productCode == item.productCode }
                                                        if(indexAllSlotDropFailCheck!=-1) {
                                                            listSlotDropFail[indexAllSlotDropFailCheck].inventory++
                                                        } else {
                                                            item.inventory = 1
                                                            item.messDrop = "NOT FOUND ANY SLOT HAVE PRODUCT CODE IS ${item.productCode}"
                                                            listSlotDropFail.add(item)
                                                        }
                                                        logger.debug("Not found ${item.productCode} in slot at local!")
                                                    }
                                                }
                                            }
                                            logger.debug("FAIL: $result")
                                        }
                                    }
                                } else {
                                    listSlotNotFound.add(item)
                                    val indexAllSlotDropFail = listAllSlotDropFail.indexOfFirst { it.productCode == item.productCode }
                                    if(indexAllSlotDropFail!=-1) {
                                        listAllSlotDropFail[indexAllSlotDropFail].inventory++
                                    } else {
                                        item.slot = -1
                                        item.inventory = 1
                                        item.messDrop = "NOT FOUND ANY SLOT HAVE PRODUCT CODE IS ${item.productCode}"
                                        listAllSlotDropFail.add(item)
                                    }

                                    val indexAllSlotDropFailCheck = listSlotDropFail.indexOfFirst { it.productCode == item.productCode }
                                    if(indexAllSlotDropFailCheck!=-1) {
                                        listSlotDropFail[indexAllSlotDropFailCheck].inventory++
                                    } else {
                                        item.inventory = 1
                                        item.messDrop = "NOT FOUND ANY SLOT HAVE PRODUCT CODE IS ${item.productCode}"
                                        listSlotDropFail.add(item)
                                    }
                                }
                                if(checkDropProductFailAll) {
                                    val tmpSlot = item
                                    val indexSlotTmp = listSlotDropFail.indexOfFirst { it.slot == tmpSlot.slot }
                                    if (indexSlotTmp!=-1) {
                                        listSlotDropFail[indexSlotTmp].inventory++
                                        listSlotDropFail[indexSlotTmp].messDrop = messDropFailed
                                    } else {
                                        tmpSlot.inventory = 1
                                        tmpSlot.messDrop = messDropFailed
                                        listSlotDropFail.add(tmpSlot)
                                    }
                                }
                            }
                        }
                    }
                }

                logger.info("==============================")
                logger.info("listSlotDropSuccess: $listSlotDropSuccess")
                logger.info("listSlotDropFail: $listSlotDropFail")
                logger.info("listAllSlotDropFail: $listAllSlotDropFail")

                val quantityNotDropped = quantityNeedDrop - quantityDropped
                logger.debug("cash dropped ${cashDropped}")
                val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
                    path = pathFileSlot
                )!!
                for (item in listSlot) {
                    if (item.inventory > 0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
                        val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
                        if (index == -1) {
                            listSlotShowInHome.add(item)
                        } else {
                            listSlotShowInHome[index].inventory += item.inventory
                        }
                    }
                }

                if(quantityDropped!=0) {
                    // Update promotion
                    if(_state.value.promotion!=null) {
                        val listCartExtra: ArrayList<CartExtra> = arrayListOf()
                        for(item in listSlotInCart) {
                            val cartExtra = CartExtra(
                                productCode = item.productCode,
                                productName = item.productName,
                                price = item.price,
                                quantity = item.inventory,
                                discount = _state.value.promotion!!.totalDiscount ?: 0,
                                amount = _state.value.promotion!!.totalAmount ?: _state.value.totalAmount,
                            )
                            listCartExtra.add(cartExtra)
                        }
                        val extra = Extra(
                            carts = listCartExtra,
                            totalAmount = _state.value.promotion!!.totalAmount ?: _state.value.totalAmount,
                            totalDiscount = _state.value.promotion!!.totalDiscount ?: 0,
                            paymentAmount = _state.value.promotion!!.paymentAmount ?: _state.value.totalAmount,
                            rewardValue = _state.value.promotion!!.rewardValue ?: -1,
                            rewardMaxValue = _state.value.promotion!!.rewardMaxValue ?: "",
                            machineCode = _state.value.initSetup!!.vendCode,
                        )
                        var listUpdatePromotion: ArrayList<LogUpdatePromotion>? = baseRepository.getDataFromLocal(
                            type = object : TypeToken<ArrayList<LogUpdatePromotion>>() {}.type,
                            path = pathFileUpdatePromotion
                        )
                        val logUpdatePromotion = LogUpdatePromotion(
                            machineCode = _state.value.initSetup!!.vendCode,
                            androidId = _state.value.initSetup!!.androidId,
                            campaignId = if(_state.value.promotion!=null)  _state.value.promotion!!.campaignId ?: "" else "",
                            voucherCode = if(_state.value.promotion!=null) _state.value.promotion!!.voucherCode ?: "" else "",
                            orderCode = _state.value.orderCode,
                            promotionId = if(_state.value.promotion!=null) _state.value.promotion!!.promotionId ?: "" else "",
                            status = true,
                            extra = extra.toBase64(),
                            isSent = false,
                        )
                        if(listUpdatePromotion.isNullOrEmpty()) {
                            listUpdatePromotion = arrayListOf()
                        }
                        listUpdatePromotion.add(logUpdatePromotion)
                        baseRepository.writeDataToLocal(
                            listUpdatePromotion,
                            pathFileUpdatePromotion
                        )
                    }
                }
                if(listAllSlotDropFail.isNotEmpty()) {
                    for(item in listAllSlotDropFail) {
                        val tmp = ProductSyncOrderRequest(
                            productCode = item.productCode,
                            productName = item.productName,
                            price = item.price.toString(),
                            quantity = item.inventory,
                            discount =if(_state.value.promotion!=null) _state.value.promotion!!.totalDiscount else 0,
                            amount = (item.inventory*item.price).toString(),
                            deliveryStatus = "failed",
                            slot = item.slot,
                            cabinetCode = "MT01",
                            deliveryStatusNote = item.messDrop,
                        )
                        _state.value.logSyncOrder!!.productDetails.add(tmp)
                    }
                    logger.info("list log sync order 11: "+_state.value.logSyncOrder!!.productDetails)
                }
                if(listSlotDropSuccess.isNotEmpty()) {
                    logger.info("list slot drop success: "+listSlotDropSuccess.toString())
                    val logSyncOrder = LogSyncOrder(
                        machineCode = _state.value.initSetup!!.vendCode,
                        orderCode = _state.value.logSyncOrder!!.orderCode,
                        androidId = _state.value.initSetup!!.androidId,
                        orderTime = _state.value.logSyncOrder!!.orderTime,
                        totalAmount = _state.value.logSyncOrder!!.totalAmount,
                        totalDiscount = _state.value.logSyncOrder!!.totalDiscount,
                        paymentAmount = _state.value.logSyncOrder!!.paymentAmount,
                        paymentMethodId = _state.value.logSyncOrder!!.paymentMethodId,
                        paymentTime = _state.value.logSyncOrder!!.paymentTime,
                        timeSynchronizedToServer = _state.value.logSyncOrder!!.timeSynchronizedToServer,
                        timeReleaseProducts = _state.value.logSyncOrder!!.timeReleaseProducts,
                        rewardType = _state.value.logSyncOrder!!.rewardType,
                        rewardValue = _state.value.logSyncOrder!!.rewardValue.toString(),
                        rewardMaxValue = _state.value.logSyncOrder!!.rewardMaxValue,
                        paymentStatus = "success",
                        deliveryStatus = "success",
                        voucherCode = _state.value.logSyncOrder!!.voucherCode,
                        productDetails = arrayListOf(),
                        isSent = false,
                    )
                    for(item in listSlotDropSuccess) {
                        val tmp = ProductSyncOrderRequest(
                            productCode = item.productCode,
                            productName = item.productName,
                            price = item.price.toString(),
                            quantity = item.inventory,
                            discount =if(_state.value.promotion!=null) _state.value.promotion!!.totalDiscount else 0,
                            amount = (item.inventory*item.price).toString(),
                            deliveryStatus = "success",
                            slot = item.slot,
                            cabinetCode = "MT01",
                            deliveryStatusNote = item.messDrop,
                        )
                        logSyncOrder.productDetails.add(tmp)
                        _state.value.logSyncOrder!!.productDetails.add(tmp)
                    }
                    logger.debug("log sync order size: ${_state.value.logSyncOrder!!.productDetails.size}")
                    logger.debug("log syncorder 1: ${_state.value.logSyncOrder}")
                    listLogSyncOrderTransaction.add(logSyncOrder)
                }

                // Sync order
                var listSyncOrder: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                    path = pathFileSyncOrder,
                )
                if(listSyncOrder.isNullOrEmpty()) {
                    listSyncOrder = arrayListOf()
                }
                listSyncOrder.add(_state.value.logSyncOrder!!)
                baseRepository.writeDataToLocal(listSyncOrder, pathFileSyncOrder)
                baseRepository.writeDataToLocal(listLogSyncOrderTransaction, pathFileSyncOrderTransaction)
                if(_state.value.nameMethodPayment != "cash") {
                    // Update delivery status
                    var listUpdateDeliveryStatus: ArrayList<LogUpdateDeliveryStatus>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogUpdateDeliveryStatus>>() {}.type,
                        path = pathFileUpdateDeliveryStatus
                    )
                    if(listUpdateDeliveryStatus.isNullOrEmpty()) {
                        listUpdateDeliveryStatus = arrayListOf()
                    }
                    for(item in listSlotDropFail) {
                        val logUpdateDeliveryStatus = LogUpdateDeliveryStatus(
                            machineCode = _state.value.initSetup!!.vendCode,
                            androidId = _state.value.initSetup!!.androidId,
                            orderCode = _state.value.orderCode,
                            deliveryStatus = "failed",
                            productCode = item.productCode,
                            deliveryStatusNote = item.messDrop,
                            slot = item.slot,
                            isSent = false,
                        )
                        listUpdateDeliveryStatus.add(logUpdateDeliveryStatus)
                    }
                    for(item in listSlotDropSuccess) {
                        val logUpdateDeliveryStatus = LogUpdateDeliveryStatus(
                            machineCode = _state.value.initSetup!!.vendCode,
                            androidId = _state.value.initSetup!!.androidId,
                            orderCode = _state.value.orderCode,
                            deliveryStatus = "success",
                            productCode = item.productCode,
                            deliveryStatusNote = "",
                            slot = item.slot,
                            isSent = false,
                        )
                        listUpdateDeliveryStatus.add(logUpdateDeliveryStatus)
                    }
                    baseRepository.writeDataToLocal(
                        listUpdateDeliveryStatus,
                        pathFileUpdateDeliveryStatus
                    )
                }
                if (quantityNotDropped != 0) {
                    if(listSlotNotFound.isNotEmpty()) {
                        logger.debug("Danh sách slot không tìm thấy")
                    }
                    if(listSlotDropFail.isNotEmpty()) {
                        logger.debug("Danh sách slot rớt lỗi")
                    }
                    var titleWarning = ""
                    if (sensorHasAnObstruction) {
                        titleWarning = "Cảm biến rơi hiện đang bị che. "
                    }
                    var cashReturnOnline = 0

                    val initSetup = _state.value.initSetup
                    val promotion = _state.value.promotion
                    if(_state.value.nameMethodPayment=="cash") {
                        val currentCash = initSetup!!.currentCash
                        if(promotion!= null) {
                            if(promotion.rewardType=="percent") {
                                if(listSlotDropSuccess.size != 0) {
                                    var cashNotDroppedTmp = 0
                                    for(item in listSlotDropFail) {
                                        cashNotDroppedTmp += item.inventory * item.price
                                    }
                                    logger.debug("cash not dropped 1: ${cashNotDroppedTmp}")
                                    cashNotDroppedTmp /= promotion.rewardValue!!
                                    logger.debug("cash not dropped 2: ${cashNotDroppedTmp}")
                                    val currentCashTmp = currentCash - _state.value.totalAmount + cashNotDroppedTmp
                                    logger.debug("cash not dropped 3: ${currentCashTmp}")
                                    initSetup.currentCash = currentCashTmp
                                    logger.debug("cashNotDroppedTmp: ${cashNotDroppedTmp}, currentCashTmp: ${currentCashTmp}")
                                    logger.debug("_state.value.totalAmount: ${_state.value.totalAmount}, _state.value.promotion: ${_state.value.promotion!!}")
                                }
                            } else {
                                var totalAmountTmp = 0
                                for(item in listSlotDropFail) {
                                    totalAmountTmp += (item.inventory * item.price)
                                }
                                val totalDiscountTmp = _state.value.promotion!!.totalDiscount
                                var totalAmountNotDropTmp = 0
                                for(item in listSlotDropFail) {
                                    totalAmountNotDropTmp += ((totalDiscountTmp!!*(item.price/totalDiscountTmp))*item.inventory)
                                }
                                logger.debug("totalAmountTmp: ${totalAmountTmp}, totalDiscountTmp: ${totalDiscountTmp}, totalAmountNotDropTmp: ${totalAmountNotDropTmp}")
                                initSetup.currentCash = currentCash - _state.value.promotion!!.paymentAmount!! + totalAmountNotDropTmp
                                logger.debug("_state.value.totalAmount: ${_state.value.totalAmount}, _state.value.promotion: ${_state.value.promotion!!}")
                            }

                        }
                        else {
                            var cashNotDroppedTmp = 0
                            for(item in listSlotDropFail) {
                                cashNotDroppedTmp += item.inventory * item.price
                            }
                            val currentCashTmp = currentCash - _state.value.totalAmount + cashNotDroppedTmp
                            initSetup.currentCash = currentCashTmp
                            logger.debug("_state.value.totalAmount: ${_state.value.totalAmount}, cashNotDroppedTmp: ${cashNotDroppedTmp}")
                        }
                        baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
                    } else {
                        if(promotion!= null) {
                            if(promotion.rewardType=="percent") {
                                for(item in listSlotDropFail) {
                                    cashReturnOnline += item.inventory * item.price
                                }
                                cashReturnOnline /= promotion.rewardValue!!
                            } else {
                                var totalAmountTmp = 0
                                for(item in listSlotDropFail) {
                                    totalAmountTmp += (item.inventory * item.price)
                                }
                                val totalDiscountTmp = _state.value.promotion!!.totalDiscount
                                for(item in listSlotDropFail) {
                                    cashReturnOnline += ((totalDiscountTmp!!*(item.price/totalDiscountTmp))*item.inventory)
                                }
                            }
                        } else {
                            var cashNotDroppedTmp = 0
                            for(item in listSlotDropFail) {
                                cashNotDroppedTmp += item.inventory * item.price
                            }
                            cashReturnOnline = _state.value.totalAmount - cashNotDroppedTmp
                        }
                        baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
                    }
                    if(_state.value.nameMethodPayment=="cash") {
                        titleWarning+="Có ${quantityNotDropped} sản phẩm rớt không thành công! Vui lòng chọn và mua lại sản phẩm khác hoặc Bấm nút \"Hoàn Tiền\". Chi tiết liên hệ 1900.99.99.80"
                    } else {
                        titleWarning+="Có ${quantityNotDropped} sản phẩm rớt không thành công! Trong vòng 7 ngày làm việc sẽ tự động hoàn tiền lại tài khoản của quý khách . Chi tiết liên hệ 1900.99.99.80"
                    }
                    _state.update {
                        it.copy(
                            isShowWaitForDropProduct = false,
                            listSlotInCard = arrayListOf(),
                            listSlotInHome = listSlotShowInHome,
                            initSetup = initSetup,
                            isWarning = true,
                            titleDialogWarning = titleWarning,
                            listSlot = listSlot,
                        )
                    }
                }
                else {
                    val initSetup = _state.value.initSetup
                    val promotion = _state.value.promotion
                    if(_state.value.nameMethodPayment=="cash") {
                        val currentCash = initSetup!!.currentCash
                        if(promotion!= null) {
                            val currentCashTmp = currentCash - promotion.paymentAmount!!
                            initSetup.currentCash = currentCashTmp
                        } else {
                            val currentCashTmp = currentCash - _state.value.totalAmount
                            initSetup.currentCash = currentCashTmp
                        }
                        baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
                    }
                    _state.update {
                        it.copy (
                            isShowWaitForDropProduct = false,
                            listSlotInCard = arrayListOf(),
                            listSlotInHome = listSlotShowInHome,
                            initSetup = initSetup,
                            listSlot = listSlot,
                            promotion = null,
                        )
                    }
                }
            } catch (e: Exception) {
                logger.error("drop product fail in HomeViewModel/dropProduct(): ${e.message}")
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "drop product fail in HomeViewModel/dropProduct(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(
                    isVendingMachineBusy = false,
                    promotion = null,
                ) }
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


    private fun processDataFromCashBox(dataByteArray: ByteArray) {
        try {
            val dataHexString = byteArrayToHexString(dataByteArray)
//            Logger.info("-------> data from cash box: $dataHexString")
            if(dataHexString.contains("01,01,03,00,00,")) {
                _numberRottenBoxBalance.value = -1
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
                _numberRottenBoxBalance.value = numberRottenBoxBalance
            } else if (dataByteArray.size == 19) {
                _isCashBoxNormal.value = true
                if (dataByteArray[6] == 0x00.toByte()) {
                    if(dataHexString != _dataTmpCashBox.value) {
                        if(_state.value.isVendingMachineBusy) {
                            portConnectionDatasource.sendCommandCashBox(byteArrays.cbReject)
                        } else {
                            _dataTmpCashBox.value = dataHexString
                            portConnectionDatasource.sendCommandCashBox(byteArrays.cbStack)
                            when (dataByteArray[7]) {
                                0x03.toByte() -> processingCash(5000)
                                0x04.toByte() -> processingCash(10000)
                                0x05.toByte() -> processingCash(20000)
                                0x06.toByte() -> processingCash(50000)
                                0x07.toByte() -> processingCash(100000)
                                0x08.toByte() -> processingCash(200000)
                                0x09.toByte() -> processingCash(500000)
                            }
                        }
                    }
                } else {
                    _isCashBoxNormal.value = true
//                    logger.debug("poll status: $dataHexString")
                    if(dataByteArray[6] == 0xFF.toByte()) {
                        if(dataHexString != _dataTmpCashBox.value) {
                            _dataTmpCashBox.value = dataHexString
                            when (dataByteArray[7]) {
                                0x0A.toByte() -> processingWhenCashBoxHasProblem("Recycled module jam problem")
                                0x0B.toByte() -> processingWhenCashBoxHasProblem("Recycled module is disconnected")
                                0x09.toByte() -> processingWhenCashBoxHasProblem("Recycled module motor problem")
                                0x08.toByte() -> processingWhenCashBoxHasProblem("Recycled module sensor problem")
                                0x07.toByte() -> processingWhenCashBoxHasProblem("Stacker faulty")
                                0x06.toByte() -> processingWhenCashBoxHasProblem("Stacker remove")
                                0x05.toByte() -> processingWhenCashBoxHasProblem("Bill Reject")
                                0x04.toByte() -> processingWhenCashBoxHasProblem("Bill Remove")
                                0x03.toByte() -> processingWhenCashBoxHasProblem("Bill Jam")
                                0x02.toByte() -> processingWhenCashBoxHasProblem("Sensor problem")
                                0x01.toByte() -> processingWhenCashBoxHasProblem("Motor problem")
                            }
                        }
                    }
                }
            } else if(dataByteArray.size == 6) {
                _setupCashBox.value = false
                if(dataHexString == "01,00,03,00,01,03") {
                    _setupCashBox.value = true
                }
            } else {
//                logger.debug("xcbndtỷtyutrygjytj: $dataHexString")
            }


            // Update the state
//            _state.update { currentState ->
//                currentState.copy(cashBoxData = byteArrayData)
//            }

        } catch (e: Exception) {
            Logger.error("Error processing cash box data: ${e.message}", e)
        }
    }

    private fun processingWhenCashBoxHasProblem(message: String) {
        viewModelScope.launch {
            logger.debug("ERRRRRRRRRRRRRRRRRRRRRRRRRRR: $message")
            baseRepository.addNewErrorLogToLocal(
                machineCode = _state.value.initSetup!!.vendCode,
                errorContent = "cash box error: $message",
                severity = "highest"
            )
        }

    }

    private fun processingCash(cash: Int) {
        viewModelScope.launch {
            try {
                logger.debug("cash = $cash")
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                baseRepository.addNewDepositWithdrawLogToLocal(
                    machineCode = initSetup.vendCode,
                    transactionType = "deposit",
                    denominationType = cash,
                    currentBalance = initSetup.currentCash,
                )
                initSetup.currentCash += cash
                baseRepository.writeDataToLocal(
                    data = initSetup,
                    path = pathFileInitSetup,
                )
                _state.update { it.copy(initSetup = initSetup) }
            } catch (e: Exception) {
                logger.debug("error: ${e.message}")
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushLogToServer() {
        logger.debug("pushLogToServer")
        viewModelScope.launch {
            try {
                val listLogServerLocalCheck: ArrayList<LogsLocal>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogsLocal>>() {}.type,
                    path = pathFileLogServer
                )
                val listLogServerLocalCheckTmp: ArrayList<LogsLocal> = arrayListOf()
                if(!listLogServerLocalCheck.isNullOrEmpty()) {
                    for(item in listLogServerLocalCheck) {
                        if(!item.isSent) {
                            listLogServerLocalCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listLogServerLocalCheckTmp, pathFileLogServer)
                }
                val listUpdateInventoryCheck: ArrayList<LogUpdateInventory>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogUpdateInventory>>() {}.type,
                    path = pathFileLogUpdateInventoryServer
                )
                val listUpdateInventoryCheckTmp: ArrayList<LogUpdateInventory> = arrayListOf()
                if(!listUpdateInventoryCheck.isNullOrEmpty()) {
                    for(item in listUpdateInventoryCheck) {
                        if(!item.isSent) {
                            listUpdateInventoryCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listUpdateInventoryCheckTmp, pathFileLogUpdateInventoryServer)
                }
                val listDepositWithdrawCheck: ArrayList<LogDepositWithdraw>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogDepositWithdraw>>() {}.type,
                    path = pathFileLogDepositWithdrawServer
                )
                val listDepositWithdrawCheckTmp: ArrayList<LogDepositWithdraw> = arrayListOf()
                if(!listDepositWithdrawCheck.isNullOrEmpty()) {
                    for(item in listDepositWithdrawCheck) {
                        if(!item.isSent) {
                            listDepositWithdrawCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listDepositWithdrawCheckTmp, pathFileLogDepositWithdrawServer)
                }
                val listSyncOrderCheck: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                    path = pathFileSyncOrder,
                )
                val listSyncOrderCheckTmp: ArrayList<LogSyncOrder> = arrayListOf()
                if(!listSyncOrderCheck.isNullOrEmpty()) {
                    for(item in listSyncOrderCheck) {
                        if(!item.isSent) {
                            listSyncOrderCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listSyncOrderCheckTmp, pathFileSyncOrder)
                }
                val listUpdatePromotionCheck: ArrayList<LogUpdatePromotion>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogUpdatePromotion>>() {}.type,
                    path = pathFileUpdatePromotion,
                )
                val listUpdatePromotionCheckTmp: ArrayList<LogUpdatePromotion> = arrayListOf()
                if(!listUpdatePromotionCheck.isNullOrEmpty()) {
                    for(item in listUpdatePromotionCheck) {
                        if(!item.isSent) {
                            listUpdatePromotionCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listUpdatePromotionCheckTmp, pathFileUpdatePromotion)
                }
                val listUpdateDeliveryStatusCheck: ArrayList<LogUpdateDeliveryStatus>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogUpdateDeliveryStatus>>() {}.type,
                    path = pathFileUpdateDeliveryStatus,
                )
                val listUpdateDeliveryStatusCheckTmp: ArrayList<LogUpdateDeliveryStatus> = arrayListOf()
                if(!listUpdateDeliveryStatusCheck.isNullOrEmpty()) {
                    for(item in listUpdateDeliveryStatusCheck) {
                        if(!item.isSent) {
                            listUpdateDeliveryStatusCheckTmp.add(item)
                        }
                    }
                    baseRepository.writeDataToLocal(listUpdateDeliveryStatusCheckTmp, pathFileUpdateDeliveryStatus)
                }
            } catch(e: Exception) {
                logger.debug("error when remove item")
            }

            if(baseRepository.isHaveNetwork(context)) {
                try {
                    logger.debug("11111")
                    val listLogServerLocal: ArrayList<LogsLocal>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogsLocal>>() {}.type,
                        path = pathFileLogServer
                    )
                    logger.debug("22222")
                    if(!listLogServerLocal.isNullOrEmpty()) {
                        val listLogServer: ArrayList<LogServer> = arrayListOf()
                        for(item in listLogServerLocal) {
                            if(!item.isSent) {
                                val logServer = LogServer(
                                    eventId = item.eventId,
                                    eventType = item.eventType,
                                    severity = item.severity,
                                    eventTime = item.eventTime,
                                    eventData = item.eventData,
                                )
                                listLogServer.add(logServer)
                            }
                        }
                        if(listLogServer.isNotEmpty()) {
                            if(baseRepository.isHaveNetwork(context)) {
                                val response = homeRepository.logMulti(listLogServer)
                                logger.debug("response: $response")
                                for(itemTmp in listLogServerLocal) {
                                    if(!itemTmp.isSent) {
                                        itemTmp.isSent = true
                                    }
                                }
                                baseRepository.writeDataToLocal(listLogServerLocal, pathFileLogServer)
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push log to server1: ${e.message}")
                }

                try {
                    val listUpdateInventory: ArrayList<LogUpdateInventory>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogUpdateInventory>>() {}.type,
                        path = pathFileLogUpdateInventoryServer
                    )
                    if(!listUpdateInventory.isNullOrEmpty()) {
                        for(item in listUpdateInventory) {
                            if(!item.isSent) {
                                val logServer = UpdateInventoryRequest(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    androidId = _state.value.initSetup!!.androidId,
                                    productList = item.productList,
                                )
                                if(baseRepository.isHaveNetwork(context)) {
                                    val response = homeRepository.updateInventory(logServer)
                                    if(response.code==200) {
                                        item.isSent = true
                                        baseRepository.writeDataToLocal(listUpdateInventory, pathFileLogUpdateInventoryServer)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push log to server2: ${e.message}")
                }

                try {
                    val listDepositWithdraw: ArrayList<LogDepositWithdraw>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogDepositWithdraw>>() {}.type,
                        path = pathFileLogDepositWithdrawServer
                    )
                    if(!listDepositWithdraw.isNullOrEmpty()) {
                        for(item in listDepositWithdraw) {
                            if(!item.isSent) {
                                val depositAndWithdrawMoneyRequest = DepositAndWithdrawMoneyRequest(
                                    machineCode = item.vendCode,
                                    androidId = _state.value.initSetup!!.androidId,
                                    transactionType = item.transactionType,
                                    denominationType = item.denominationType.toString(),
                                    quantity = item.quantity.toString(),
                                    currentBalance = item.currentBalance.toString(),
                                    synTime = item.synTime,
                                )
                                try {
                                    val response = homeRepository.pushDepositWithdrawToServer(depositAndWithdrawMoneyRequest)
                                    logger.debug("===== response withdraw-deposit: ${response.toString()}")
                                    item.isSent = true
                                    baseRepository.writeDataToLocal(listDepositWithdraw, pathFileLogDepositWithdrawServer)
                                } catch(e: Exception) {
                                    logger.debug("error withdraw/deposit: ${e.message}")
                                    baseRepository.addNewErrorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        errorContent = "upload deposit/withdraw to server failed: ${e.message}",
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push log to server3: ${e.message}")
                }

                try {
                    val listSyncOrder: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
                        path = pathFileSyncOrder,
                    )
                    if(!listSyncOrder.isNullOrEmpty()) {
                        for(item in listSyncOrder) {
                            if(!item.isSent) {
                                val dataSyncOrderRequest = DataSyncOrderRequest(
                                    machineCode = item.machineCode,
                                    orderCode = item.orderCode,
                                    androidId = item.androidId,
                                    orderTime = item.orderTime,
                                    totalAmount = item.totalAmount,
                                    totalDiscount = item.totalDiscount,
                                    paymentAmount = item.paymentAmount,
                                    paymentMethodId = item.paymentMethodId,
                                    paymentTime = item.paymentTime,
                                    timeSynchronizedToServer = item.timeSynchronizedToServer,
                                    timeReleaseProducts = item.timeReleaseProducts,
                                    rewardType = item.rewardType,
                                    rewardValue = item.rewardValue,
                                    rewardMaxValue = item.rewardMaxValue,
                                    paymentStatus = item.paymentStatus,
                                    deliveryStatus = item.deliveryStatus,
                                    voucherCode = item.voucherCode,
                                    productDetails = item.productDetails,
                                )
                                val orders: ArrayList<DataSyncOrderRequest> = arrayListOf()
                                orders.add(dataSyncOrderRequest)
                                val syncOrderRequest = SyncOrderRequest(
                                    orders = orders,
                                )
                                try {
                                    val response = homeRepository.syncOrder(syncOrderRequest)
                                    logger.debug("===== response sync order: ${response.toString()}")
                                    item.isSent = true
                                    baseRepository.writeDataToLocal(listSyncOrder, pathFileSyncOrder)
                                } catch(e: Exception) {
                                    logger.debug("error log sync order: ${e.message}")
                                    baseRepository.addNewErrorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        errorContent = "upload log sync order to server failed: ${e.message}",
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push sync order log to server: ${e.message}")
                }

                try {
                    val listUpdatePromotion: ArrayList<LogUpdatePromotion>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogUpdatePromotion>>() {}.type,
                        path = pathFileUpdatePromotion,
                    )
                    if(!listUpdatePromotion.isNullOrEmpty()) {
                        for(item in listUpdatePromotion) {
                            if(!item.isSent) {
                                val updatePromotionRequest = UpdatePromotionRequest(
                                    machineCode = item.machineCode,
                                    orderCode = item.orderCode,
                                    androidId = item.androidId,
                                    extra = item.extra,
                                    status = item.status,
                                    voucherCode = item.voucherCode,
                                    promotionId = item.promotionId,
                                    campaignId = item.campaignId,
                                )
                                try {
                                    val response = homeRepository.updatePromotion(updatePromotionRequest)
                                    logger.debug("===== response update promotion: ${response.toString()}")
                                    item.isSent = true
                                    baseRepository.writeDataToLocal(listUpdatePromotion, pathFileUpdatePromotion)
                                } catch(e: Exception) {
                                    baseRepository.addNewErrorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        errorContent = "upload log update promotion to server failed: ${e.message}",
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push log update promotion to server: ${e.message}")
                }

                try {
                    val listUpdateDeliveryStatus: ArrayList<LogUpdateDeliveryStatus>? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<ArrayList<LogUpdateDeliveryStatus>>() {}.type,
                        path = pathFileUpdateDeliveryStatus,
                    )
                    if(!listUpdateDeliveryStatus.isNullOrEmpty()) {
                        for(item in listUpdateDeliveryStatus) {
                            if(!item.isSent) {
                                val updateDeliveryStatus = UpdateDeliveryStatusRequest(
                                    machineCode = item.machineCode,
                                    orderCode = item.orderCode,
                                    androidId = item.androidId,
                                    deliveryStatus = item.deliveryStatus,
                                    productCode = item.productCode,
                                    deliveryStatusNote = item.deliveryStatusNote,
                                    slot = item.slot,
                                )
                                try {
                                    val response = homeRepository.updateDeliveryStatus(updateDeliveryStatus)
                                    logger.debug("===== response update delivery status: ${response.toString()}")
                                    item.isSent = true
                                    baseRepository.writeDataToLocal(listUpdateDeliveryStatus, pathFileUpdateDeliveryStatus)
                                } catch(e: Exception) {
                                    baseRepository.addNewErrorLogToLocal(
                                        machineCode = _state.value.initSetup!!.vendCode,
                                        errorContent = "upload log update delivery status to server failed: ${e.message}",
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("error in push log update delivery status to server: ${e.message}")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushDepositWithdrawToServer() {
        logger.debug("pushDepositWithdrawToServer")
        viewModelScope.launch {
            try {
                val listDepositWithdraw: ArrayList<LogDepositWithdraw> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogDepositWithdraw>>() {}.type,
                    path = pathFileLogDepositWithdrawServer
                )!!
                if(listDepositWithdraw.isNotEmpty()) {
                    for(item in listDepositWithdraw) {
                        if(!item.isSent) {
                            val depositAndWithdrawMoneyRequest = DepositAndWithdrawMoneyRequest(
                                machineCode = item.vendCode,
                                androidId = _state.value.initSetup!!.androidId,
                                transactionType = item.transactionType,
                                denominationType = item.denominationType.toString(),
                                quantity = item.quantity.toString(),
                                currentBalance = item.currentBalance.toString(),
                                synTime = item.synTime,
                            )
                            try {
                                val response = homeRepository.pushDepositWithdrawToServer(depositAndWithdrawMoneyRequest)
                                logger.debug("===== response withdraw-deposit: ${response.toString()}")
                                item.isSent = true
                                baseRepository.writeDataToLocal(listDepositWithdraw, pathFileLogDepositWithdrawServer)
                            } catch(e: Exception) {
                                logger.debug("error withdraw/deposit: ${e.message}")
                                baseRepository.addNewErrorLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    errorContent = "upload deposit/withdraw to server failed: ${e.message}",
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.debug("error in push log to server: ${e.message}")
            }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun pushSyncOrderToServer() {
//        logger.debug("pushSyncOrderToServer")
//        viewModelScope.launch {
//            try {
//                val listSyncOrder: ArrayList<LogSyncOrder>? = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<ArrayList<LogSyncOrder>>() {}.type,
//                    path = pathFileSyncOrder,
//                )
//                if(!listSyncOrder.isNullOrEmpty()) {
//                    for(item in listSyncOrder) {
//                        if(!item.isSent) {
//                            val syncOrderRequest = DataSyncOrderRequest(
//                                machineCode = item.machineCode,
//                                orderCode = item.orderCode,
//                                androidId = item.androidId,
//                                orderTime = item.orderTime,
//                                totalAmount = item.totalAmount,
//                                totalDiscount = item.totalDiscount,
//                                paymentAmount = item.paymentAmount,
//                                paymentMethodId = item.paymentMethodId,
//                                paymentTime = item.paymentTime,
//                                timeSynchronizedToServer = item.timeSynchronizedToServer,
//                                timeReleaseProducts = item.timeReleaseProducts,
//                                rewardType = item.rewardType,
//                                rewardValue = item.rewardValue,
//                                rewardMaxValue = item.rewardMaxValue,
//                                paymentStatus = item.paymentStatus,
//                                deliveryStatus = item.deliveryStatus,
//                                voucherCode = item.voucherCode,
//                                productDetails = item.productDetails,
//                            )
//                            try {
//                                val response = homeRepository.syncOrder(syncOrderRequest)
//                                logger.debug("===== response sync order: ${response.toString()}")
//                                item.isSent = true
//                                baseRepository.writeDataToLocal(listSyncOrder, pathFileSyncOrder)
//                            } catch(e: Exception) {
//                                logger.debug("error log sync order: ${e.message}")
//                                baseRepository.addNewErrorLogToLocal(
//                                    machineCode = _state.value.initSetup!!.vendCode,
//                                    errorContent = "upload log sync order to server failed: ${e.message}",
//                                )
//                            }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                logger.debug("error in push sync order log to server: ${e.message}")
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushUpdatePromotionToServer() {
        logger.debug("pushUpdatePromotionToServer")
        viewModelScope.launch {
            try {
                val listUpdatePromotion: ArrayList<LogUpdatePromotion>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogUpdatePromotion>>() {}.type,
                    path = pathFileUpdatePromotion,
                )
                if(!listUpdatePromotion.isNullOrEmpty()) {
                    for(item in listUpdatePromotion) {
                        if(!item.isSent) {
                            val updatePromotionRequest = UpdatePromotionRequest(
                                machineCode = item.machineCode,
                                orderCode = item.orderCode,
                                androidId = item.androidId,
                                extra = item.extra,
                                status = item.status,
                                voucherCode = item.voucherCode,
                                promotionId = item.promotionId,
                                campaignId = item.campaignId,
                            )
                            try {
                                val response = homeRepository.updatePromotion(updatePromotionRequest)
                                logger.debug("===== response update promotion: ${response.toString()}")
                                item.isSent = true
                                baseRepository.writeDataToLocal(listUpdatePromotion, pathFileUpdatePromotion)
                            } catch(e: Exception) {
                                baseRepository.addNewErrorLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    errorContent = "upload log update promotion to server failed: ${e.message}",
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.debug("error in push log update promotion to server: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pushUpdateDeliveryStatusToServer() {
        logger.debug("pushUpdateDeliveryStatusToServer")
        viewModelScope.launch {
            try {
                val listUpdateDeliveryStatus: ArrayList<LogUpdateDeliveryStatus>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<LogUpdateDeliveryStatus>>() {}.type,
                    path = pathFileUpdateDeliveryStatus,
                )
                if(!listUpdateDeliveryStatus.isNullOrEmpty()) {
                    for(item in listUpdateDeliveryStatus) {
                        if(!item.isSent) {
                            val updateDeliveryStatus = UpdateDeliveryStatusRequest(
                                machineCode = item.machineCode,
                                orderCode = item.orderCode,
                                androidId = item.androidId,
                                deliveryStatus = item.deliveryStatus,
                                productCode = item.productCode,
                                deliveryStatusNote = item.deliveryStatusNote,
                                slot = item.slot,
                            )
                            try {
                                val response = homeRepository.updateDeliveryStatus(updateDeliveryStatus)
                                logger.debug("===== response update delivery status: ${response.toString()}")
                                item.isSent = true
                                baseRepository.writeDataToLocal(listUpdateDeliveryStatus, pathFileUpdateDeliveryStatus)
                            } catch(e: Exception) {
                                baseRepository.addNewErrorLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    errorContent = "upload log update delivery status to server failed: ${e.message}",
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
            logger.debug("error in push log update delivery status to server: ${e.message}")
        }
        }
    }

//    fun getNetworkStatus(): String {
//        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connectivityManager.activeNetwork ?: return "No connection"
//        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"
//
//        return when {
//            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
//            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
//            else -> "Unknown"
//        }
//    }

    fun getNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"
        val networkType = when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            else -> "Unknown"
        }
        return networkType
    }

    fun getNetworkStatus(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"
        val downlinkSpeed = networkCapabilities.linkDownstreamBandwidthKbps
        val uplinkSpeed = networkCapabilities.linkUpstreamBandwidthKbps
        return "Downlink: ${downlinkSpeed / 1000} Mbps, Uplink: ${uplinkSpeed / 1000} Mbps"
    }

    private fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(",") { "%02X".format(it) }
    }

    fun hexStringToByteArray(hexString: String): ByteArray {
        return hexString.split(",")
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

//    private fun startReadingThread() {
//        readThreadCashBox.start()
//    }
//
//    // Read thread cash box
//    private val readThreadCashBox = object : Thread() {
//        override fun run() {
//            Logger.info("PortConnectionDataSource: start read thread cash box")
//            while (!currentThread().isInterrupted) {
//                try {
//                    portConnectionHelperDatasource.startReadingCashBox(512) { data ->
//                        val dataHexString = byteArrayToHexString(data)
//                        Logger.info("PortConnectionDataSource: data is $dataHexString")
//                        viewModelScope.launch {
//                            _cashBoxData.emit(dataHexString)
//                        }
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    break
//                }
//            }
//        }
//    }
//
//    private fun byteArrayToHexString(byteArray: ByteArray): String {
//        return byteArray.joinToString(",") { "%02X".format(it) }
//    }

    fun loadInitData() {
        logger.info("loadInitData")
        viewModelScope.launch {
            try {
                // Get init setup in local
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
//                initSetup.currentCash = 50000
                // Get list method payment
                val listPaymentMethod: ArrayList<PaymentMethodResponse> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<PaymentMethodResponse>>() {}.type,
                    path = pathFilePaymentMethod
                )!!
                // Get list path ads
                var listAds = homeRepository.getListVideoAdsFromLocal()
                if(!baseRepository.isFileExists(pathFileUpdateTrackingAds())){
                    baseRepository.createFolder(pathFileUpdateTrackingAds())
//                    baseRepository.writeDataToLocal(pathFileUpdateTrackingAds,)
                }
                if (listAds.isEmpty()) {
                    homeRepository.writeVideoAdsFromAssetToLocal(
                        context,
                        R.raw.ads1,
                        "ads1.mp4",
                        pathFolderAds,
                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads2,
//                        "ads2.mp4",
//                        pathFolderAds,
//                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads3,
//                        "ads3.mp4",
//                        pathFolderAds,
//                    )
                    listAds = homeRepository.getListVideoAdsFromLocal()
                }
                // Get list path ads
                var listBigAds = homeRepository.getListVideoBigAdsFromLocal()
                if (listBigAds.isEmpty()) {
                    homeRepository.writeVideoAdsFromAssetToLocal(
                        context,
                        R.raw.ads2,
                        "ads2.mp4",
                        pathFolderBigAds,
                    )
                    listBigAds = homeRepository.getListVideoAdsFromLocal()
                }
                // Get list slot in local
                val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
                    path = pathFileSlot
                )!!
                // Get list slot in home
                val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
                for(item in listSlot) {
                    if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
                        val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
                        if (index == -1) {
                            listSlotShowInHome.add(item)
                        } else {
                            listSlotShowInHome[index].inventory += item.inventory
                        }
                    }
                }
                _state.update {
                    it.copy(
                        initSetup = initSetup,
                        listAds = listAds,
                        listBigAds = listBigAds,
                        listSlot = listSlot,
                        listSlotInHome = listSlotShowInHome,
                        listPaymentMethod = listPaymentMethod,
                        countDownPaymentByCash = initSetup.timeoutPaymentByCash.toLong(),
                    )
                }
                portConnectionDatasource.openPortCashBox(initSetup.portCashBox)
                if(!portConnectionDatasource.checkPortCashBoxStillStarting()) {
                    portConnectionDatasource.startReadingCashBox()
                }
                portConnectionDatasource.openPortVendingMachine(initSetup.portVendingMachine,initSetup.typeVendingMachine)
                if(!portConnectionDatasource.checkPortVendingMachineStillStarting()) {
                    portConnectionDatasource.startReadingVendingMachine()
                }
                observePortData()
                _setupCashBox.value = false
                sendCommandCashBox(byteArrays.cbEnableType3456789)
                delay(300)
                if(_setupCashBox.value) {
                    logger.debug("set cbEnableType3456789 success")
                } else {
                    logger.debug("set cbEnableType3456789 fail")
                }
                _setupCashBox.value = false
                sendCommandCashBox(byteArrays.cbSetRecyclingBillType4)
                delay(300)
                if(_setupCashBox.value) {
                    logger.debug("set cbSetRecyclingBillType4 success")
                } else {
                    logger.debug("set cbSetRecyclingBillType4 fail")
                }
                _setupCashBox.value = false
                sendCommandCashBox(byteArrays.cbEscrowOn)
                delay(300)
                if(_setupCashBox.value) {
                    logger.debug("set cbEscrowOn success")
                } else {
                    logger.debug("set cbEscrowOn fail")
                }
                sendCommandVendingMachine(byteArrays.vmReadTemp)
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "load init data fail in HomeViewModel/loadInitData(): ${e.message}",
                )
            }
        }
    }

//    fun getListPathAdsFromLocal() {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//                delay(5000)
//                var listAds = homeRepository.getListVideoAdsFromLocal()
//                if (listAds.isEmpty()) {
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads1,
//                        "ads1.mp4",
//                        pathFolderAds,
//                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads2,
//                        "ads2.mp4",
//                        pathFolderAds,
//                    )
//                    homeRepository.writeVideoAdsFromAssetToLocal(
//                        context,
//                        R.raw.ads3,
//                        "ads3.mp4",
//                        pathFolderAds,
//                    )
//                    listAds = homeRepository.getListVideoAdsFromLocal()
//                }
//                _state.update { it.copy(
//                    listAds = listAds,
//                    isLoading = false,
//                ) }
//            } catch (e: Exception) {
//                logger.info("Error video ads: ${e.message}")
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }

    fun showAdsDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            showAds()
        }
    }

    fun showAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = true) }
        }
    }

    fun hideAdsDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            hideAds()
        }
    }

//    fun hideDropFailDebounced() {
//        debounceJob?.cancel()
//        debounceJob = viewModelScope.launch {
//            delay(debounceDelay)
//            hideDropFail()
//        }
//    }

//    fun hideDropFail() {
//        viewModelScope.launch {
//            _state.update { it.copy(isShowDropFail = false) }
//        }
//    }

    fun hideAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowAds = false) }
        }
    }

    fun showBigAds() {
        viewModelScope.launch {
            _state.update { it.copy(isShowBigAds = true) }
        }
    }

    fun hideBigAds() {
        logger.debug("hideAds")
        viewModelScope.launch {
            _state.update { it.copy(isShowBigAds = false) }
        }
    }

    fun hideShowQrCode() {
        logger.debug("hideShowQrCode")
        viewModelScope.launch {
            countdownTimer?.cancel()
            countdownTimerCallApi?.cancel()
            _state.update { it.copy(
                isShowQrCode = false,
                promotion = null,
            ) }
        }
    }

    fun backDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            backInCartPayment()
        }
    }

    fun backInCartPayment() {
        viewModelScope.launch {
            _state.update { it.copy(
                isShowCart = false,
                promotion = null,
            ) }
        }
    }

    fun backInPayment() {
        viewModelScope.launch {
            countdownTimer?.cancel()
            countdownTimer = null
            _state.update { it.copy(
                isShowPushMoney = false,
                promotion = null,
            ) }
        }
    }

    fun chooseAnotherMethodPayment() {
        viewModelScope.launch {
            countdownTimer?.cancel()
            countdownTimer = null
            _state.update { it.copy(
                isShowCart = true,
                isShowPushMoney = false
            ) }
        }
    }

    fun showPaymentDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            showPayment()
        }
    }

    fun showPayment() {
        viewModelScope.launch {
            try {
                _state.update { it.copy (
                    isShowCart = true,
                    isLoading = true,
                    promotion = null,
                    nameMethodPayment = "cash",
                    logSyncOrder = null,
                    orderCode = "",
                ) }
                var listPaymentMethod: ArrayList<PaymentMethodResponse>? = baseRepository.getDataFromLocal(
                    type = object : TypeToken<ArrayList<PaymentMethodResponse>>() {}.type,
                    path = pathFilePaymentMethod
                )
                val totalAmount = homeRepository.getTotalAmount(_state.value.listSlotInCard)
                if(listPaymentMethod.isNullOrEmpty()) {
                    _state.update { it.copy (
                        listPaymentMethod = arrayListOf(),
                        totalAmount = totalAmount,
                        nameMethodPayment = "",
                    ) }
                } else {
                    _isCashBoxNormal.value = false
                    sendCommandCashBox(byteArrays.cbPollStatus)
                    delay(300)
                    if(!_isCashBoxNormal.value) {
                        sendCommandCashBox(byteArrays.cbPollStatus)
                        delay(300)
                        if(!_isCashBoxNormal.value) {
                            sendCommandCashBox(byteArrays.cbPollStatus)
                            delay(300)
                            if(!_isCashBoxNormal.value) {
                                for(item in listPaymentMethod) {
                                    if(item.methodName == "cash") {
                                        if(_state.value.initSetup!!.currentCash < totalAmount) {
                                            listPaymentMethod.remove(item)
                                            _state.update { it.copy (
                                                nameMethodPayment = "",
                                            ) }
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!baseRepository.isHaveNetwork(context)) {
                        var paymentMethodResponse: PaymentMethodResponse? = null
                        for(item in listPaymentMethod) {
                            logger.debug("item $item")
                            if(item.methodName == "cash") {
                                paymentMethodResponse = item
                                break
                            }
                        }
                        listPaymentMethod = arrayListOf()
                        if(paymentMethodResponse != null) {
                            listPaymentMethod.add(paymentMethodResponse)
                        }
                    }
                    _state.update { it.copy (listPaymentMethod = listPaymentMethod) }
                    if(baseRepository.isHaveNetwork(context)) {
                        if(_state.value.initSetup!!.initPromotion == "ON") {
                            try {
                                val promotion = homeRepository.getPromotion(
                                    voucherCode = _state.value.voucherCode,
                                    listSlot = _state.value.listSlotInCard,
                                )
                                logger.debug("999: promotion: $promotion")
                                _state.update { it.copy (
                                    promotion = promotion,
                                    totalAmount = promotion.totalAmount ?: totalAmount,
                                ) }
                            } catch (e: Exception) {
                                logger.debug("error in show payment: ${e.message}")
                                baseRepository.addNewErrorLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    errorContent = "get init promotion fail HomeViewModel/showPayment(): ${e.message}",
                                )
                                _state.update { it.copy (
                                    totalAmount = totalAmount,
                                ) }
                            }
                        } else {
                            _state.update { it.copy (
                                totalAmount = totalAmount,
                            ) }
                        }
                    } else {
                        _state.update { it.copy (
                            totalAmount = totalAmount,
                        ) }
                    }
                }
            } catch (e: Exception) {
                logger.debug("error in show payment: ${e.message}")
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "show payment fail in HomeViewModel/showPayment(): ${e.message}",
                )
            } finally {
                _state.update { it.copy (isLoading = false) }
            }
        }
    }

    fun updateNameMethod(nameMethod: String) {
        viewModelScope.launch {
            if(_isCashBoxNormal.value) {
                _state.update { it.copy (nameMethodPayment = nameMethod) }
            } else {
                if(nameMethod!="cash") {
                    _state.update { it.copy (nameMethodPayment = nameMethod) }
                } else {
                    sendEvent(Event.Toast("Phương thức thanh toán tiền mặt hiện không khả dung!"))
                }
            }
        }
    }

    fun pollStatus() {
//        logger.debug("pollStatus")
        viewModelScope.launch {
            portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
        }
    }

    fun readDoor() {
//        logger.debug("readDoor")
        viewModelScope.launch {
            portConnectionDatasource.sendCommandVendingMachine(byteArrays.vmReadDoor)
        }
    }

    fun checkPort() {
        logger.debug("checkPort")
        viewModelScope.launch {
            val byteArrayTmp = byteArrayOf(0xFA.toByte(), 0xFB.toByte(), 0x41.toByte(), 0x00.toByte(), 0x40.toByte())
            val byteArrayTmpString = byteArrayToHexString(byteArrayTmp)
            logger.debug("byteArrayTmpString: $byteArrayTmpString")
            val commandBytes = byteArrayOf(0x71.toByte(), 0x85.toByte(), 0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte())
            portConnectionDatasource.sendCommandVendingMachine(commandBytes)
        }
    }

    fun xoay() {
        logger.debug("pollStatus")
        viewModelScope.launch {
            try {
                portConnectionDatasource.sendCommandCashBox(ByteArrays().cbPollStatus)
            } catch (e: Exception) {
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getPowerInfo(): PowerInfo {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = (level / scale.toFloat() * 100).toInt()

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Not charging"
        }

        return PowerInfo(batteryLevel = batteryPct, isCharging = isCharging, chargingSource = chargingSource)
    }

    fun writeLogStatusNetworkAndPower() {
        logger.debug("writeLogStatusNetworkAndPower")
        viewModelScope.launch {
            try {
                val networkType = getNetworkType()
                logger.debug("networkType: $networkType")
                val networkStatus = getNetworkStatus()
                logger.debug("networkStatus: $networkStatus")
                val powerInfo = getPowerInfo()
                logger.debug("powerInfo: ${powerInfo.isCharging}, ${powerInfo.chargingSource}, ${powerInfo.batteryLevel}")
                val ipAddress = getIpAddress()
                logger.debug("ipAddress: $ipAddress")
                baseRepository.addNewStatusLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    networkType = networkType,
                    ip = ipAddress ?: "",
                    networkStatus = networkStatus,
                    powerInfo = "isCharging: ${powerInfo.isCharging}, chargingSource: ${powerInfo.chargingSource}, batteryLevel: ${powerInfo.batteryLevel}",
                )
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "write log status network and power fail in HomeViewModel/writeLogStatusNetworkAndPower(): ${e.message}",
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("DefaultLocale")
    private fun getWifiIpAddress(): String {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
    }

    private fun getMobileIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val sAddr = address.hostAddress
                        // Only return IPv4 address
                        if (sAddr != null) {
                            if (sAddr.indexOf(':') < 0) {
                                return sAddr
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return "Unable to get IP address"
    }


    fun getIpAddress(): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No connection"
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return "No connection"

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> getWifiIpAddress()
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getMobileIpAddress()
            else -> "Unknown"
        }
    }

    fun getTotalAmount() {
        viewModelScope.launch {
            try {
                val totalAmount = homeRepository.getTotalAmount(_state.value.listSlotInCard)
                _state.update { it.copy (totalAmount = totalAmount) }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "get total amount fail in HomeViewModel/getTotalAmount(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }

    fun addProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            addProduct(slot)
        }
    }

    fun addProduct(slot: Slot) {
        logger.info("addProduct")
        viewModelScope.launch {
            try {
                _state.update { currentState ->
                    val listSlotInCart = ArrayList(currentState.listSlotInCard)
                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                    if (index == -1) {
                        val tmpSlot = slot.copy(inventory = 1)
                        listSlotInCart.add(tmpSlot)
                    } else {
                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                        listSlotInCart[index] = updatedSlot
                    }
                    currentState.copy(
                        listSlotInCard = listSlotInCart,
                        slotAtBottom = listSlotInCart.lastOrNull()
                    )
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "add product fail in HomeViewModel/addProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            }
        }
    }
    

    fun minusProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            minusProduct(slot)
        }
    }

    fun minusProduct(slot: Slot) {
        logger.info("minusProduct")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
//                _state.update { currentState ->
//                    var check = false
//                    val listSlotInCart = ArrayList(currentState.listSlotInCard)
//                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
//                    if (index != -1) {
//                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory - 1)
//                        if (updatedSlot.inventory == 0) {
//                            listSlotInCart.removeAt(index)
//                            check = true
//                        } else {
//                            listSlotInCart[index] = updatedSlot
//                        }
//                    }
//                    if(listSlotInCart.isEmpty()) {
//                        currentState.copy(
//                            listSlotInCard = listSlotInCart,
//                            slotAtBottom = listSlotInCart.lastOrNull(),
//                            isShowCart = false,
//                            totalAmount = 0,
//                        )
//                    } else {
//                        var total = 0
//                        for(item in listSlotInCart) {
//                            total+=(item.inventory*item.price)
//                        }
//                        if(_state.value.isShowCart) {
//                            if (_state.value.nameMethodPayment != "cash") {
//                                if (!_isCashBoxNormal.value) {
//                                    if(_state.value.initSetup!!.currentCash >= total) {
//                                        val listMethodPayment = _state.value.listPaymentMethod
//                                        var check = false
//                                        for(item in listMethodPayment) {
//                                            if(item.methodName == "cash") {
//                                                check = true
//                                            }
//                                        }
//                                        if(!check) {
//                                            val itemPaymentMethod = PaymentMethodResponse(
//                                                methodName = "cash",
//                                                brief = "Tiền Mặt",
//                                                isMustOnline = "no",
//                                                imageUrl = "https://i.imgur.com/xNRXpXu.png",
//                                                status = 1,
//                                                id = 4,
//                                                storeId = null,
//                                            )
//                                            listMethodPayment.add(itemPaymentMethod)
//                                            currentState.copy(
//                                                listPaymentMethod = listMethodPayment,
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        currentState.copy(
//                            listSlotInCard = listSlotInCart,
//                            slotAtBottom = if(check) listSlotInCart.lastOrNull() else listSlotInCart[index],
//                            totalAmount = total,
//                        )
//                    }
//                }
                var check = false
                val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                if (index != -1) {
                    val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory - 1)
                    if (updatedSlot.inventory == 0) {
                        listSlotInCart.removeAt(index)
                        check = true
                    } else {
                        listSlotInCart[index] = updatedSlot
                    }
                }
                if(listSlotInCart.isEmpty()) {
                    _state.update { it.copy(
                        listSlotInCard = listSlotInCart,
                        slotAtBottom = listSlotInCart.lastOrNull(),
                        isShowCart = false,
                        totalAmount = 0,
                        promotion = null,
                    ) }
//                    currentState.copy(
//                        listSlotInCard = listSlotInCart,
//                        slotAtBottom = listSlotInCart.lastOrNull(),
//                        isShowCart = false,
//                        totalAmount = 0,
//                    )
                } else {
                    var total = 0
                    for(item in listSlotInCart) {
                        total+=(item.inventory*item.price)
                    }
                    if(_state.value.isShowCart) {
                        if (_state.value.nameMethodPayment != "cash") {
                            if (!_isCashBoxNormal.value) {
                                if(_state.value.initSetup!!.currentCash >= total) {
                                    val listMethodPayment = _state.value.listPaymentMethod
                                    var check = false
                                    for(item in listMethodPayment) {
                                        if(item.methodName == "cash") {
                                            check = true
                                        }
                                    }
                                    if(!check) {
                                        val itemPaymentMethod = PaymentMethodResponse(
                                            methodName = "cash",
                                            brief = "Tiền Mặt",
                                            isMustOnline = "no",
                                            imageUrl = "https://i.imgur.com/xNRXpXu.png",
                                            status = 1,
                                            id = 4,
                                            storeId = null,
                                        )
                                        listMethodPayment.add(itemPaymentMethod)
                                        _state.update { it.copy(listPaymentMethod = listMethodPayment) }
//                                        currentState.copy(
//                                            listPaymentMethod = listMethodPayment,
//                                        )
                                    }
                                }
                            }
                        }
                    }
                    if(_state.value.promotion!=null) {
                        if(baseRepository.isHaveNetwork(context)) {
                            val promotionResponse = homeRepository.getPromotion(
                                voucherCode = _state.value.promotion!!.voucherCode!!,
                                listSlot = listSlotInCart,
                            )
                            logger.info(promotionResponse.toString())
                            _state.update { it.copy(
                                listSlotInCard = listSlotInCart,
                                slotAtBottom = if(check) listSlotInCart.lastOrNull() else listSlotInCart[index],
                                totalAmount = promotionResponse.paymentAmount ?: total,
                                promotion = promotionResponse,
                            ) }
                        }
                    } else {
                        _state.update { it.copy(
                            listSlotInCard = listSlotInCart,
                            slotAtBottom = if(check) listSlotInCart.lastOrNull() else listSlotInCart[index],
                            totalAmount = total,
                        ) }
                    }

                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "minus product fail in HomeViewModel/minusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun applyPromotionDebounced(voucherCode: String) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(200L)
            applyPromotion(voucherCode)
        }
    }

    fun applyPromotion(voucherCode: String = "") {
        logger.info("applyPromotion")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
//                if(_state.value.initSetup!!.initPromotion == "ON") {
                    if(baseRepository.isHaveNetwork(context)) {
                        val promotionResponse = homeRepository.getPromotion(
                            voucherCode = voucherCode,
                            listSlot = _state.value.listSlotInCard,
                        )
                        logger.debug("promotion response: $promotionResponse")
                        _state.update { it.copy(
                            promotion = promotionResponse,
                            totalAmount = promotionResponse.paymentAmount ?: _state.value.totalAmount,
                            isLoading = false,
                        ) }
                    } else {
                        sendEvent(Event.Toast("Not have internet, please connect with internet!"))
                        _state.update { it.copy(isLoading = false) }
                    }
//                } else {
//                    if(voucherCode!="") {
//                        if(baseRepository.isHaveNetwork(context)) {
//                            val promotionResponse = homeRepository.getPromotion(
//                                voucherCode = voucherCode,
//                                listSlot = _state.value.listSlotInCard,
//                            )
//                            logger.debug("promotion response: $promotionResponse")
//                            _state.update { it.copy(
//                                promotion = promotionResponse,
//                                isLoading = false,
//                            ) }
//                        } else {
//                            sendEvent(Event.Toast("Not have internet, please connect with internet!"))
//                            _state.update { it.copy(isLoading = false) }
//                        }
//                    } else {
//                        _state.update { it.copy(isLoading = false) }
//                    }
//                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "minus product fail in HomeViewModel/minusProduct(): ${e.message}",
                )
                sendEvent(Event.Toast("${e.message}"))
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun paymentConfirmation() {
        logger.info("paymentConfirmation")
        viewModelScope.launch {
            if(_state.value.nameMethodPayment.isEmpty()) {
                sendEvent(Event.Toast("Hãy chọn phương thức thanh toán!"))
            } else {
                try {
                    _state.update { it.copy(
                        countDownPaymentByCash = (_state.value.initSetup!!.timeoutPaymentByCash.toLong()),
                        isVendingMachineBusy = true,
                    ) }
                    val orderCode = LocalDateTime.now().toId()
                    logger.debug("android id: ${_state.value.initSetup!!.androidId}, orderCode: ${orderCode}")
                    val orderTime = LocalDateTime.now().toDateTimeString()
                    val totalAmount = if(_state.value.promotion!=null) _state.value.promotion!!.totalAmount ?: _state.value.totalAmount else _state.value.totalAmount
                    val totalDiscount = if(_state.value.promotion!=null) _state.value.promotion!!.totalDiscount ?: 0 else 0
                    val paymentAmount = if(_state.value.promotion!=null) _state.value.promotion!!.paymentAmount ?: _state.value.totalAmount else _state.value.totalAmount
                    val paymentMethodId = _state.value.nameMethodPayment
                    val rewardType = if(_state.value.promotion!=null) _state.value.promotion!!.rewardType ?: "percent" else "percent"
                    val rewardValue = if(_state.value.promotion!=null) _state.value.promotion!!.rewardValue ?: 0 else 0
                    val rewardMaxValue = if(_state.value.promotion!=null) _state.value.promotion!!.rewardMaxValue ?: 0 else 0
                    val voucherCode = if(_state.value.promotion!=null) _state.value.promotion!!.voucherCode ?: "" else ""
                    val listProductInCart = _state.value.listSlotInCard
                    val currentTime = LocalDateTime.now().toDateTimeString()
                    val productDetails: ArrayList<ProductSyncOrderRequest> = arrayListOf()
//                    for (item in listProductInCart) {
//                        val slot = homeRepository.getSlotDrop(item.productCode)
//                        val productDetailRequest = ProductSyncOrderRequest(
//                            productCode = item.productCode,
//                            productName = item.productName,
//                            price = item.price.toString(),
//                            quantity = item.inventory,
//                            discount = 0,
//                            amount = (item.inventory*item.price).toString(),
//                            slot = slot!!.slot,
//                            deliveryStatus = "",
//                            cabinetCode = "MT01",
//                            deliveryStatusNote = "",
//                        )
//                        productDetails.add(productDetailRequest)
//                    }
                    val logSyncOrder = LogSyncOrder(
                        machineCode = _state.value.initSetup!!.vendCode,
                        orderCode = orderCode,
                        androidId = _state.value.initSetup!!.androidId,
                        orderTime = currentTime,
                        totalAmount = totalAmount,
                        totalDiscount = totalDiscount,
                        paymentAmount = paymentAmount,
                        paymentMethodId = paymentMethodId,
                        paymentTime = currentTime,
                        timeSynchronizedToServer = currentTime,
                        timeReleaseProducts = currentTime,
                        rewardType = rewardType,
                        rewardValue = rewardValue.toString(),
                        rewardMaxValue = rewardMaxValue.toString().toInt() ?: 0,
                        paymentStatus = "success",
                        deliveryStatus = "success",
                        voucherCode = voucherCode,
                        productDetails = productDetails,
                        isSent = false,
                    )
                    val listProductDetailRequest: ArrayList<ProductDetailRequest> = arrayListOf()
                    when(_state.value.nameMethodPayment) {
                        "cash" -> {
                            logger.debug("method payment: cash")
                            val initSetup: InitSetup = baseRepository.getDataFromLocal(
                                type = object : TypeToken<InitSetup>() {}.type,
                                path = pathFileInitSetup
                            )!!
                            if(initSetup.currentCash >= _state.value.totalAmount) {
                                _state.update { it.copy(
                                    isShowCart = false,
                                    orderCode = orderCode,
                                    isShowWaitForDropProduct = true,
                                    logSyncOrder = logSyncOrder,
                                ) }
                                dropProduct()
                            } else {
                                _state.update { it.copy(
                                    orderCode = orderCode,
                                    isShowPushMoney = true,
                                    isShowCart = false,
                                    logSyncOrder = logSyncOrder,
                                ) }
                                startCountdownPaymentByCash()
                            }
                        }
                        "momo", "vnpay", "zalopay" -> {
                            _state.update { it.copy(isLoading = true)}
                            var storeId = ""
                            for(item in _state.value.listPaymentMethod) {
                                if(item.methodName == _state.value.nameMethodPayment) {
                                    storeId = item.storeId ?: ""
                                    break
                                }
                            }
                            for(item in listProductInCart) {
                                val slot = homeRepository.getSlotDrop(item.productCode)
                                val productDetailRequest = ProductDetailRequest(
                                    productCode = item.productCode,
                                    productName = item.productName,
                                    price = item.price,
                                    quantity = item.inventory,
                                    discount = 0,
                                    amount = item.inventory*item.price,
                                    slot = if(slot!=null) item.slot else 0,
                                    cabinetCode = "MT01"
                                )
                                listProductDetailRequest.add(productDetailRequest)
                            }
                            val request = GetQrCodeRequest(
                                machineCode = _state.value.initSetup!!.vendCode,
                                androidId = _state.value.initSetup!!.androidId,
                                orderCode = orderCode,
                                orderTime = orderTime,
                                totalAmount = totalAmount,
                                totalDiscount = totalDiscount,
                                paymentAmount = paymentAmount,
//                                totalAmount = 1000,
//                                totalDiscount = totalDiscount,
//                                paymentAmount = 1000,
                                paymentMethodId = paymentMethodId,
                                storeId = storeId,
                                productDetails = listProductDetailRequest,
                            )
                            logger.debug("method: $paymentMethodId, storeId: ${storeId}")
                            val response = homeRepository.getQrCodeFromServer(request)
                            logger.debug("response neeeeeeeeeeeeeeeeeee: $response")
                            if(response.qrCodeUrl!!.isNotEmpty()) {
                                val qrCodeBitmap = generateQrCodeBitmap(response.qrCodeUrl!!)
                                val imageBitmap = qrCodeBitmap.asImageBitmap()
                                _state.update { it.copy(
                                    isShowCart = false,
                                    isLoading = false,
                                    orderCode = orderCode,
                                    imageBitmap = imageBitmap,
                                    isShowQrCode = true,
                                    logSyncOrder = logSyncOrder,
                                ) }
                                startCountdownPaymentByOnline(orderCode = orderCode, storeId = storeId)
                            } else {
                                _state.update { it.copy(
                                    isLoading = false,
                                    isVendingMachineBusy = false,
                                )}
                            }
                        }
                    }
                } catch (e: Exception) {
                    baseRepository.addNewErrorLogToLocal(
                        machineCode = _state.value.initSetup!!.vendCode,
                        errorContent = "payment confirmation fail in HomeViewModel/paymentConfirmation(): ${e.message}",
                    )
                    _state.update { it.copy(
                        isLoading = false,
                        isVendingMachineBusy = false,
                    ) }
                } finally {
                    _state.update { it.copy(
                        isVendingMachineBusy = false,
                    ) }
                }
            }
        }
    }

    private fun startCountdownPaymentByOnline(orderCode: String, storeId: String) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer((_state.value.initSetup!!.timeoutPaymentByQrCode.toLong()*1000), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _state.update { it.copy(countDownPaymentByOnline = (millisUntilFinished / 1000).toLong()) }
            }
            override fun onFinish() {
                _state.update { it.copy(countDownPaymentByOnline = 0) }
                cancelPaymentOnline()
            }
        }.start()
        countdownTimerCallApi?.cancel()
        countdownTimerCallApi = object : CountDownTimer((_state.value.initSetup!!.timeoutPaymentByQrCode.toLong()*1000), 2000) {
            override fun onTick(millisUntilFinished: Long) {
                viewModelScope.launch {
                    try {
                        val checkResultPaymentOnline = CheckPaymentResultOnlineRequest(
                            paymentMethodId = _state.value.nameMethodPayment,
                            orderCode = orderCode,
                            storeId = storeId,
                        )
                        logger.info("Check result online: ${checkResultPaymentOnline}")
                        val resultPaymentByOnline =
                            homeRepository.checkResultPaymentOnline(checkResultPaymentOnline)
                        logger.debug("resultPaymentByOnline: $resultPaymentByOnline")
                        if(resultPaymentByOnline.returnCode!=-1) {
                            _state.update { it.copy(
                                isShowQrCode = false,
                                isShowWaitForDropProduct = true,
                            ) }
                            countdownTimer?.cancel()
                            countdownTimerCallApi?.cancel()
                            dropProduct()
                        }
                    } catch(e: Exception) {
                        logger.error("Error while check payment: ${e.message}")
                    }
                }
            }

            override fun onFinish() { }
        }.start()
    }

    private fun cancelPaymentOnline() {
        // Handle payment cancellation
        _state.update { it.copy(
            isShowQrCode = false,
            isVendingMachineBusy = false,
        ) }
    }

    fun generateQrCodeBitmap(qrCodeUrl: String): Bitmap {
        val size = 512 // Size of the QR code
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H)
        val bitMatrix = QRCodeWriter().encode(qrCodeUrl, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

//    fun showDialogConfirm(mess: String) {
//        viewModelScope.launch {
//            _state.update {
//                it.copy(
//                    titleDialogConfirm = mess,
//                    isConfirm = true,
//                )
//            }
//        }
//    }

    fun hideDialogConfirm() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isConfirm = false,
                    titleDialogConfirm = "",
                )
            }
        }
    }

    fun withdrawalMoney() {
        logger.info("withdrawalMoney")
        viewModelScope.launch {
            try {
                val initSetup: InitSetup = baseRepository.getDataFromLocal(
                    type = object : TypeToken<InitSetup>() {}.type,
                    path = pathFileInitSetup
                )!!
                val currentCash = initSetup.currentCash
                if(currentCash>0) {
                    val numberCashNeedReturn = if(currentCash>9999) {
                        currentCash/10000
                    } else {
                        0
                    }

                    if(numberCashNeedReturn>0) {
                        sendCommandCashBox(byteArrays.cbGetNumberRottenBoxBalance)
                        delay(300)
                        if(_numberRottenBoxBalance.value!=-1) {
                            if(_numberRottenBoxBalance.value>=numberCashNeedReturn) {
                                sendCommandCashBox(getCreateByteArrayDispenseBill(numberCashNeedReturn))
                                initSetup.currentCash = currentCash - (numberCashNeedReturn*10000)
                                _state.update { it.copy(initSetup = initSetup) }
                                baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
                                baseRepository.addNewDepositWithdrawLogToLocal(
                                    machineCode = _state.value.initSetup!!.vendCode,
                                    transactionType = "deposit",
                                    denominationType = 10000,
                                    quantity = numberCashNeedReturn,
                                    currentBalance = currentCash,
                                )
//                                delay(300)
//                                if(_setupCashBox.value) {
//                                    logger.debug("Ok")
//                                } else {
//                                    logger.debug("Lỗi")
//                                }
                            } else {
                                showDialogWarning("Máy hiện không đủ tiền thối. Vui lòng mua thêm sản phẩm hoặc liên hệ 1900.99.99.80 để nhận lại tiền thừa. Xin chân thành cảm ơn")
                            }
                        } else {
                            logger.debug("Lỗi")
                        }
                    } else {
                        showDialogWarning("Máy chỉ có thể thối tền mệnh giá 10.000 vnđ. Vui lòng mua thêm sản phẩm khác")
                    }
                    logger.debug("Number cash return: $numberCashNeedReturn")
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "withdrawal money fail in HomeViewModel/withdrawalMoney(): ${e.message}",
                )
                _state.update { it.copy(isLoading = false) }
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
        portConnectionDatasource.sendCommandVendingMachine(byteArray)
    }

//    fun dropProduct() {
//        logger.info("dropProduct")
//        viewModelScope.launch {
//            try {
//                val listSlotFail: ArrayList<Slot> = arrayListOf()
//                val listSlotNotFound: ArrayList<Slot> = arrayListOf()
//                val listSlotInCart = _state.value.listSlotInCard
//                var sensorHasAnObstacle = false
//                var quantityNeedDrop = 0
//                var quantityDropped = 0
//                var currentCash = _state.value.initSetup!!.currentCash
//                var cashDropped = 0
//                // Define a label for the outer loop
//                outerLoop@ for (item in listSlotInCart) {
//                    quantityNeedDrop += item.inventory
//                    logger.debug("Slot drop: $item")
//                    for (index in 1..item.inventory) {
//                        val slot = homeRepository.getSlotDrop(item.productCode)
//                        logger.debug("Slot found: $slot")
//                        if (slot != null) {
//                            _statusDropProduct.value = DropSensorResult.DEFAULT
//                            productDispense(0, slot.slot)
//                            while (_statusDropProduct.value == DropSensorResult.DEFAULT) {
//                                delay(100) // check every 100ms
//                            }
////                            delay(6000)
//                            if (_statusDropProduct.value == DropSensorResult.SUCCESS) {
//                                logger.debug("SUCCESS: ${_statusDropProduct.value}")
//                                item.inventory--
//                                cashDropped+=item.price
//                                quantityDropped++
//                            } else if (_statusDropProduct.value == DropSensorResult.SENSOR_HAS_AN_OBSTACLE) {
//                                // Break out of both loops when this condition is met
//                                sensorHasAnObstacle = true
//                                break@outerLoop
//                            } else {
//                                listSlotFail.add(slot)
//                                homeRepository.lockSlot(slot.slot)
//                                val listAnotherSlot = homeRepository.getListAnotherSlot(item.productCode)
//                                for(itemAnother in listAnotherSlot) {
//
//                                    val slotAnother = homeRepository.getSlotDrop(item.productCode)
//                                    logger.debug("slotAnother found: $slotAnother")
//                                    if(slotAnother!=null) {
//                                        productDispense(0, slotAnother.slot)
//                                        delay(6000)
//                                        if (_statusDropProduct.value == DropSensorResult.SUCCESS) {
//                                            logger.debug("SUCCESS: ${_statusDropProduct.value}")
//                                            item.inventory--
//                                            cashDropped+=item.price
//                                            quantityDropped++
//                                            break
//                                        } else if (_statusDropProduct.value == DropSensorResult.SENSOR_HAS_AN_OBSTACLE) {
//                                            // Break out of both loops when this condition is met
//                                            sensorHasAnObstacle = true
//                                            break@outerLoop
//                                        }
//                                        listSlotFail.add(slotAnother)
//                                        homeRepository.lockSlot(slotAnother.slot)
//                                    } else {
//                                        listSlotNotFound.add(item)
//                                        logger.debug("Not found ${item.productCode} in slot at local!")
//                                    }
//                                }
//                                logger.debug("FAIL: ${_statusDropProduct.value}")
//                            }
//                            _statusDropProduct.value = DropSensorResult.DEFAULT
//                        } else {
//                            listSlotNotFound.add(item)
//                            logger.debug("Not found ${item.productCode} in slot at local!")
//                        }
//                    }
//                }
//                if(sensorHasAnObstacle) {
//                    logger.debug("SENSOR HAS AN OBSTACLE")
//                }
//                val tmpQuantity = quantityNeedDrop-quantityDropped
//
//                val initSetup =_state.value.initSetup
//                initSetup!!.currentCash = currentCash-cashDropped
//                baseRepository.writeDataToLocal(initSetup, pathFileInitSetup)
//                val listSlot: ArrayList<Slot> = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
//                    path = pathFileSlot,
//                ) ?: arrayListOf()
//                val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
//                for(item in listSlot) {
//                    if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
//                        val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
//                        if (index == -1) {
//                            listSlotShowInHome.add(item)
//                        } else {
//                            listSlotShowInHome[index].inventory += item.inventory
//                        }
//                    }
//                }
//                if(tmpQuantity!=0) {
//                    logger.debug("Có ${tmpQuantity} sản phẩm rơi thất bại trả lại ${currentCash-cashDropped}")
//                    _state.update { it.copy(
//                        isShowWaitForDropProduct = false,
//                        listSlotInCard = arrayListOf(),
//                        numberProductDroppedFail = tmpQuantity,
//                        listSlotInHome = listSlotShowInHome,
//                        initSetup = initSetup,
//                        isShowDropFail = true,
//                        listSlot = listSlot,
//                    ) }
//                } else {
//                    _state.update { it.copy(
//                        isShowWaitForDropProduct = false,
//                        listSlotInCard = arrayListOf(),
//                        numberProductDroppedFail = tmpQuantity,
//                        listSlotInHome = listSlotShowInHome,
//                        initSetup = initSetup,
//                        listSlot = listSlot,
//                    ) }
//                }
//            } catch (e: Exception) {
//                baseRepository.addNewErrorLogToLocal(
//                    machineCode = _state.value.initSetup!!.vendCode,
//                    errorContent = "payment confirmation fail in HomeViewModel/paymentConfirmation(): ${e.message}",
//                )
//                sendEvent(Event.Toast("${e.message}"))
//            }
//        }
//    }

    private fun startCountdownPaymentByCash() {
        countdownTimer?.cancel() // Cancel any existing timer
        countdownTimer = object : CountDownTimer((_state.value.initSetup!!.timeoutPaymentByCash.toLong()*1000), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _state.update { it.copy(countDownPaymentByCash = (millisUntilFinished / 1000).toLong()) }
                if(_state.value.initSetup!!.currentCash >= _state.value.totalAmount) {
                    _state.update { it.copy (
                        isShowCart = false,
                        isShowPushMoney = false,
                        isShowWaitForDropProduct = true,
                    ) }
                    dropProduct()
                }
            }

            override fun onFinish() {
                _state.update { it.copy(
                    countDownPaymentByCash = 0,
                    isVendingMachineBusy = false,
                ) }
                // Handle countdown finish, e.g., cancel payment
                cancelPaymentByCash()
            }
        }.start()
    }

    private fun cancelPaymentByCash() {
        // Handle payment cancellation
        _state.update { it.copy(
            isShowPushMoney = false,
            promotion = null,
        ) }
    }

    fun getInventoryByProductCode(productCode: String): Int {
        val listSlotInCart = _state.value.listSlotInCard
        val index = listSlotInCart.indexOfFirst { it.productCode == productCode }
        return if (index == -1) {
            -1
        } else {
            listSlotInCart[index].inventory
        }
    }

    fun getTotal(): Int {
        val listSlotInCart = _state.value.listSlotInCard
        var total = 0
        for(item in listSlotInCart) {
            total+=(item.inventory*item.price)
        }
        return total
    }

//
//    fun getInventoryOfProduct(productCode: String): Int {
//        val listSlotShowInHome = _state.value.listSlotInHome
//        val index = listSlotShowInHome.indexOfFirst { it.productCode == productCode }
//        return if (index == -1) {
//            -1
//        } else {
//            listSlotShowInHome[index].inventory
//        }
//    }

    fun plusProductDebounced(slot: Slot) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(debounceDelay)
            plusProduct(slot)
        }
    }

    fun plusProduct(slot: Slot) {
        logger.info("plusProduct")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                if(_state.value.isShowCart) {
                    if(_state.value.nameMethodPayment=="cash") {
                        if(!_isCashBoxNormal.value) {
                            val total = getTotal()
                            if(_state.value.initSetup!!.currentCash >= (total+slot.price)) {
                                val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                                val indexSlotBuy = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                                val listSlotShowInHome = ArrayList(_state.value.listSlotInHome)
                                val indexSlotShowInHome = listSlotShowInHome.indexOfFirst { it.productCode == slot.productCode }
                                if (indexSlotBuy != -1 && indexSlotShowInHome != -1 && listSlotInCart[indexSlotBuy].inventory < listSlotShowInHome[indexSlotShowInHome].inventory) {
//                                    _state.update { currentState ->
//                                        val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
//                                        if (index != -1) {
//                                            val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
//                                            listSlotInCart[index] = updatedSlot
//                                        }
//                                        var total = 0
//                                        for(item in listSlotInCart) {
//                                            total+=(item.inventory*item.price)
//                                        }
//                                        currentState.copy(
//                                            listSlotInCard = listSlotInCart,
//                                            slotAtBottom = listSlotInCart[index],
//                                            totalAmount = total,
//                                        )
//                                    }
                                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                                    if (index != -1) {
                                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                                        listSlotInCart[index] = updatedSlot
                                    }
                                    var totalAmount = 0
                                    for(item in listSlotInCart) {
                                        totalAmount+=(item.inventory*item.price)
                                    }
                                    if(_state.value.promotion!=null) {
                                        if(baseRepository.isHaveNetwork(context)) {
                                            val promotionResponse = homeRepository.getPromotion(
                                                voucherCode = _state.value.promotion!!.voucherCode!!,
                                                listSlot = listSlotInCart,
                                            )
                                            logger.info(promotionResponse.toString())
                                            _state.update { it.copy(
                                                listSlotInCard = listSlotInCart,
                                                slotAtBottom = listSlotInCart[index],
                                                totalAmount = promotionResponse.paymentAmount ?: totalAmount,
                                                promotion = promotionResponse,
                                            ) }
                                        }
                                    } else {
                                        _state.update { it.copy(
                                            listSlotInCard = listSlotInCart,
                                            slotAtBottom = listSlotInCart[index],
                                            totalAmount = totalAmount,
                                        ) }
                                    }
                                }
                            }
                        } else {
                            val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                            val indexSlotBuy = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                            val listSlotShowInHome = ArrayList(_state.value.listSlotInHome)
                            val indexSlotShowInHome = listSlotShowInHome.indexOfFirst { it.productCode == slot.productCode }
                            if (indexSlotBuy != -1 && indexSlotShowInHome != -1 && listSlotInCart[indexSlotBuy].inventory < listSlotShowInHome[indexSlotShowInHome].inventory) {
//                                _state.update { currentState ->
//                                    val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
//                                    if (index != -1) {
//                                        val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
//                                        listSlotInCart[index] = updatedSlot
//                                    }
//                                    var total = 0
//                                    for(item in listSlotInCart) {
//                                        total+=(item.inventory*item.price)
//                                    }
//                                    currentState.copy(
//                                        listSlotInCard = listSlotInCart,
//                                        slotAtBottom = listSlotInCart[index],
//                                        totalAmount = total,
//                                    )
//                                }
                                val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                                if (index != -1) {
                                    val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                                    listSlotInCart[index] = updatedSlot
                                }
                                var totalAmount = 0
                                for(item in listSlotInCart) {
                                    totalAmount+=(item.inventory*item.price)
                                }
                                if(_state.value.promotion!=null) {
                                    if(baseRepository.isHaveNetwork(context)) {
                                        val promotionResponse = homeRepository.getPromotion(
                                            voucherCode = _state.value.promotion!!.voucherCode!!,
                                            listSlot = listSlotInCart,
                                        )
                                        logger.info(promotionResponse.toString())
                                        _state.update { it.copy(
                                            listSlotInCard = listSlotInCart,
                                            slotAtBottom = listSlotInCart[index],
                                            totalAmount = promotionResponse.paymentAmount ?: totalAmount,
                                            promotion = promotionResponse,
                                        ) }
                                    }
                                } else {
                                    _state.update { it.copy(
                                        listSlotInCard = listSlotInCart,
                                        slotAtBottom = listSlotInCart[index],
                                        totalAmount = totalAmount,
                                    ) }
                                }
                            }
                        }
                    } else {
                        val total = getTotal()
                        if(_state.value.initSetup!!.currentCash < (total+slot.price)) {
                            val listMethodPayment = _state.value.listPaymentMethod
                            val index = listMethodPayment.indexOfFirst { it.methodName == "cash"}
                            if(index!=-1) {
                                listMethodPayment.removeAt(index)
                                _state.update { currentState ->
                                    currentState.copy(
                                        listPaymentMethod = listMethodPayment,
                                    )
                                }
                            }
                        }
                        val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                        val indexSlotBuy = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                        val listSlotShowInHome = ArrayList(_state.value.listSlotInHome)
                        val indexSlotShowInHome = listSlotShowInHome.indexOfFirst { it.productCode == slot.productCode }
                        if (indexSlotBuy != -1 && indexSlotShowInHome != -1 && listSlotInCart[indexSlotBuy].inventory < listSlotShowInHome[indexSlotShowInHome].inventory) {
//                            _state.update { currentState ->
//                                val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
//                                if (index != -1) {
//                                    val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
//                                    listSlotInCart[index] = updatedSlot
//                                }
//                                var total = 0
//                                for(item in listSlotInCart) {
//                                    total+=(item.inventory*item.price)
//                                }
//                                currentState.copy(
//                                    listSlotInCard = listSlotInCart,
//                                    slotAtBottom = listSlotInCart[index],
//                                    totalAmount = total,
//                                )
//                            }
                            val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                            if (index != -1) {
                                val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                                listSlotInCart[index] = updatedSlot
                            }
                            var totalAmount = 0
                            for(item in listSlotInCart) {
                                totalAmount+=(item.inventory*item.price)
                            }
                            if(_state.value.promotion!=null) {
                                if(baseRepository.isHaveNetwork(context)) {
                                    val promotionResponse = homeRepository.getPromotion(
                                        voucherCode = _state.value.promotion!!.voucherCode!!,
                                        listSlot = listSlotInCart,
                                    )
                                    logger.info(promotionResponse.toString())
                                    _state.update { it.copy(
                                        listSlotInCard = listSlotInCart,
                                        slotAtBottom = listSlotInCart[index],
                                        totalAmount = promotionResponse.paymentAmount ?: totalAmount,
                                        promotion = promotionResponse,
                                    ) }
                                }
                            } else {
                                _state.update { it.copy(
                                    listSlotInCard = listSlotInCart,
                                    slotAtBottom = listSlotInCart[index],
                                    totalAmount = totalAmount,
                                ) }
                            }
                        }
                    }
                } else {
                    val listSlotInCart = ArrayList(_state.value.listSlotInCard)
                    val indexSlotBuy = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                    val listSlotShowInHome = ArrayList(_state.value.listSlotInHome)
                    val indexSlotShowInHome = listSlotShowInHome.indexOfFirst { it.productCode == slot.productCode }
                    if (indexSlotBuy != -1 && indexSlotShowInHome != -1 && listSlotInCart[indexSlotBuy].inventory < listSlotShowInHome[indexSlotShowInHome].inventory) {
//                        _state.update { currentState ->
//                            val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
//                            if (index != -1) {
//                                val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
//                                listSlotInCart[index] = updatedSlot
//                            }
//                            var total = 0
//                            for(item in listSlotInCart) {
//                                total+=(item.inventory*item.price)
//                            }
//                            currentState.copy(
//                                listSlotInCard = listSlotInCart,
//                                slotAtBottom = listSlotInCart[index],
//                                totalAmount = total,
//                            )
//                        }
                        val index = listSlotInCart.indexOfFirst { it.productCode == slot.productCode }
                        if (index != -1) {
                            val updatedSlot = listSlotInCart[index].copy(inventory = listSlotInCart[index].inventory + 1)
                            listSlotInCart[index] = updatedSlot
                        }
                        var totalAmount = 0
                        for(item in listSlotInCart) {
                            totalAmount+=(item.inventory*item.price)
                        }
                        if(_state.value.promotion!=null) {
                            if(baseRepository.isHaveNetwork(context)) {
                                val promotionResponse = homeRepository.getPromotion(
                                    voucherCode = _state.value.promotion!!.voucherCode!!,
                                    listSlot = listSlotInCart,
                                )
                                logger.info(promotionResponse.toString())
                                _state.update { it.copy(
                                    listSlotInCard = listSlotInCart,
                                    slotAtBottom = listSlotInCart[index],
                                    totalAmount = promotionResponse.paymentAmount ?: totalAmount,
                                    promotion = promotionResponse,
                                ) }
                            }
                        } else {
                            _state.update { it.copy(
                                listSlotInCard = listSlotInCart,
                                slotAtBottom = listSlotInCart[index],
                                totalAmount = totalAmount,
                            ) }
                        }
                    }
                }

            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = _state.value.initSetup!!.vendCode,
                    errorContent = "plus product fail in HomeViewModel/plusProduct(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

//    fun paymentNow() {
//        viewModelScope.launch {
//            sendEvent(Event.NavigateToHomeScreen)
//        }
//    }
//
//    fun getListSlotFromLocal() {
//        logger.debug("getListSlotFromLocal")
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//                val listSlot: ArrayList<Slot>? = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<ArrayList<Slot>>() {}.type,
//                    path = pathFileSlot
//                )
//                if(listSlot.isNullOrEmpty()) {
//                    _state.update {
//                        it.copy(
//                            listSlot = arrayListOf(),
//                            listSlotInHome = arrayListOf(),
//                            isLoading = false,
//                        )
//                    }
//                } else {
//                    val listSlotShowInHome: ArrayList<Slot> = arrayListOf()
//                    for(item in listSlot) {
//                        if(item.inventory>0 && item.productCode.isNotEmpty() && !item.isLock && item.productName.isNotEmpty()) {
//                            val index = listSlotShowInHome.indexOfFirst { it.productCode == item.productCode }
//                            if (index == -1) {
//                                listSlotShowInHome.add(item)
//                            } else {
//                                listSlotShowInHome[index].inventory += item.inventory
//                            }
//                        }
//                    }
//                    _state.update {
//                        it.copy(
//                            listSlot = listSlot,
//                            listSlotInHome = listSlotShowInHome,
//                            isLoading = false,
//                        )
//                    }
//                }
//            } catch (e: Exception) {
//                val initSetup: InitSetup = baseRepository.getDataFromLocal(
//                    type = object : TypeToken<InitSetup>() {}.type,
//                    path = pathFileInitSetup
//                )!!
//                val logError = LogError(
//                    machineCode = initSetup.vendCode,
//                    errorType = "application",
//                    errorContent = "get list slot from local fail in HomeViewModel/getListSlotFromLocal: ${e.message}",
//                    eventTime = LocalDateTime.now().toDateTimeString(),
//                )
//                baseRepository.addNewLogToLocal(
//                    eventType = "error",
//                    severity = "normal",
//                    eventData = logError,
//                )
//                sendEvent(Event.Toast("${e.message}"))
//                _state.update { it.copy(isLoading = false) }
//            }
//        }
//    }
}
data class PowerInfo(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val chargingSource: String
)
