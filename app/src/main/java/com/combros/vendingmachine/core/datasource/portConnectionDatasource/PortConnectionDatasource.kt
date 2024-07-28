package com.combros.vendingmachine.core.datasource.portConnectionDatasource

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import com.combros.vendingmachine.core.util.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
data class DataRxCommunicateTTS4(
    var typeRXCommunicateAvf: TypeRXCommunicateAvf = TypeRXCommunicateAvf.UNKNOWN,
    var data: String = ""
)
enum class TypeTXCommunicateAvf {
    PRODUCT_DISPENSE,
    SENSOR_DETECT_DROP_PRODUCT,
    SET_ONE_SLOT_AS_SINGLE_SLOT,
    SET_ONE_SLOT_AS_DOUBLE_SLOT,
    SET_ALL_SLOT_AS_SINGLE_SLOT,
    ENQUIRY_SLOT,
    TEMPERATURE_CONTROL,
    SET_MODE_REFRIGERATING,
    SET_MODE_HEATING,
    TEMPERATURE_READ,
    TIME_OUT_DROP_SENSOR,
    TEST_TIME_OUT,
    SET_COMPRESSOR_DEFROST_TIME,
    SET_COMPRESSOR_WORKING_TIME,
    SET_DOWN_TIME,
    INITIAL_GLASS_HEAT,
    DISABLE_GLASS_HEAT,
    LIGHT_ON,
    LIGHT_OFF,
    READ_DOOR,
    SET_MORE_DROP,
    UNKNOWN,
}
enum class TypeRXCommunicateAvf {
    SUCCESS,
    FAIL,
    DROP_PRODUCT_SUCCESS,
    ROTATE_FAIL,
    ROTATE_FAIL_DROP_SENSOR_FAIL,
    ROTATE_SUCCESS_NO_PRODUCT_VIBRATE,
    ROTATE_SUCCESS_PRODUCT_VIBRATE,
    PMOS_FAIL,

    DROP_SENSOR_NOT_FOUND,
    DROP_SENSOR_ERROR,
    SLOT_NOT_FOUND,
    TEMPERATURE_READ,
    TEMPERATURE_READ_T1,
    TEMPERATURE_READ_T2,
    DOOR_OPEN,
    TIME_OUT,

    UNKNOWN,
}

class PortConnectionDatasource {
    // CoroutineScope (Dispatchers.IO + SupervisorJob())
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Port connection helper
    private val portConnectionHelperDataSource = PortConnectionHelperDatasource()

    // Data from cash box
    private val _dataFromCashBox = MutableStateFlow<ByteArray>(byteArrayOf())
    val dataFromCashBox: StateFlow<ByteArray> = _dataFromCashBox
    // Data from vending machine
    private val _dataFromVendingMachine = MutableStateFlow<ByteArray>(byteArrayOf())
    val dataFromVendingMachine: StateFlow<ByteArray> = _dataFromVendingMachine

    // Data from vending machine
    private val _listSerialPort = MutableStateFlow<Array<String>>(arrayOf())
    val listSerialPort: StateFlow<Array<String>> = _listSerialPort

    // Status of vending machine
    private var fdPortVendingMachine: Int = -1
    // Status of cash box
    private var fdPortCashBox: Int = -1
    private var typeTXCommunicateAvf = TypeTXCommunicateAvf.UNKNOWN
    private var typeRXCommunicateAvf = TypeRXCommunicateAvf.UNKNOWN
    private var timerCommunicateTTYS4: CountDownTimer? = null
    private val listeners = mutableListOf<TypeRXCommunicateAvfListener>()
    private var callbackDeferredTTS4: CompletableDeferred<DataRxCommunicateTTS4>? = null
    private var portVendingString =""
    // Open port vending machine
//    fun openPortVendingMachine(port: String, typeVendingMachine: String = "TCN") : Int {
//        if(typeVendingMachine == "TCN") {
//            Logger.debug("TCN")
//            fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 9600)
//        } else {
//            Logger.debug("XY")
//            fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 57600)
//        }
//        Logger.debug("open port vending machine: $fdPortVendingMachine")
//        return fdPortVendingMachine
//    }

    fun openPortVendingMachine(port: String, typeVendingMachine: String = "TCN") : Int {
//        if (typeVendingMachine == "TCN") {
//            Logger.debug("TCN")
//            fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 9600)
//        } else {
//            Logger.debug("XY")
//            fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 57600)
//        }
        fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 9600)
        Logger.debug("open port vending machine: $fdPortVendingMachine")
        return fdPortVendingMachine
    }

    fun openUsbPortVendingMachine(port: String, typeVendingMachine: String = "TCN") : Int {
        fdPortVendingMachine = portConnectionHelperDataSource.openUsbPortVendingMachine("/dev/", port, 9600)
        Logger.debug("open port vending machine: $fdPortVendingMachine")
        return fdPortVendingMachine
    }

    // Close vending machine ports
    fun closeUsbPortVendingMachine() {
        portConnectionHelperDataSource.closeUsbPortVendingMachine()
        Logger.info("PortConnectionDataSource: port vending machine is disconnected")
    }

    fun getListSerialPort(): Array<String> {
        return portConnectionHelperDataSource.getAllSerialPorts();
    }
    fun getListSerialPortStatus(): Array<Pair<String, Boolean>> {
        return portConnectionHelperDataSource.getAllSerialPortsStatus();
    }
    // Open port cash box
    fun openPortCashBox(port: String) : Int {
        fdPortCashBox = portConnectionHelperDataSource.openPortCashBox("/dev/", port, 9600)
        Logger.debug("open port cash box: $fdPortCashBox")
        return fdPortCashBox
    }

    // Close vending machine ports
    fun closeVendingMachinePort() {
        portConnectionHelperDataSource.closePortVendingMachine()
        Logger.info("PortConnectionDataSource: port vending machine is disconnected")
    }
    // Close cash box ports
    fun closeCashBoxPort() {
        portConnectionHelperDataSource.closePortCashBox()
        Logger.info("PortConnectionDataSource: port cash box is disconnected")
    }

    // Start reading vending machine ports
    fun startReadingVendingMachine() {
        readThreadVendingMachine.start()
    }
    // Start reading vending machine ports
    fun startReadingUsbVendingMachine() {
        readUsbThreadVendingMachine.start()
    }
    // Start reading cash box ports
    fun startReadingCashBox() {
        readThreadCashBox.start()
    }

    fun checkPortCashBoxStillStarting(): Boolean {
        if(readThreadCashBox.isAlive) {
            return true
        }
        return false
    }
    fun checkPortVendingMachineStillStarting(): Boolean {
        if(readThreadVendingMachine.isAlive) {
            return true
        }
        return false
    }

    // Read thread vending machine
    private val readUsbThreadVendingMachine = object : Thread() {
        @SuppressLint("SuspiciousIndentation")
        override fun run() {
            Logger.info("PortConnectionDataSource: start read thread vending machine")
            while (!currentThread().isInterrupted) {
                try {
                    portConnectionHelperDataSource.startReadingUsbVendingMachine(512) { data ->
                        Logger.info("-------> data from vending machine: ${byteArrayToHexString(data)}")
                        coroutineScope.launch {
                            _dataFromVendingMachine.emit(data)
                            val receivedText = byteArrayToHexString(data)
                            when (receivedText) {
                                "00,5D,00,00,5D" ->
                                    typeRXCommunicateAvf = TypeRXCommunicateAvf.SUCCESS
                                "00,5C,00,00,5C" -> {
                                    if(typeTXCommunicateAvf == TypeTXCommunicateAvf.ENQUIRY_SLOT){
                                        typeRXCommunicateAvf = TypeRXCommunicateAvf.SLOT_NOT_FOUND
                                    }
                                }
                            }
                            cancelTimerCommunicateTTYS4()
                            callbackDeferredTTS4?.complete(
                                DataRxCommunicateTTS4(
                                    typeRXCommunicateAvf,
                                    receivedText
                                )
                            )
                            callbackDeferredTTS4 = null
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    // Read thread vending machine
    private val readThreadVendingMachine = object : Thread() {
        @SuppressLint("SuspiciousIndentation")
        override fun run() {
            Logger.info("PortConnectionDataSource: start read thread vending machine")
            while (!currentThread().isInterrupted) {
                try {
                    portConnectionHelperDataSource.startReadingVendingMachine(512) { data ->
                        Logger.info("-------> data from vending machine: ${byteArrayToHexString(data)}")
                        coroutineScope.launch {
                            _dataFromVendingMachine.emit(data)
                            val receivedText = byteArrayToHexString(data)
                            when (receivedText) {
                                "00,5D,00,00,5D" ->
                                    typeRXCommunicateAvf = TypeRXCommunicateAvf.SUCCESS
                                "00,5C,00,00,5C" -> {
                                    if(typeTXCommunicateAvf == TypeTXCommunicateAvf.ENQUIRY_SLOT){
                                        typeRXCommunicateAvf = TypeRXCommunicateAvf.SLOT_NOT_FOUND
                                    }
                                }
                            }
                            cancelTimerCommunicateTTYS4()
                            callbackDeferredTTS4?.complete(
                                DataRxCommunicateTTS4(
                                    typeRXCommunicateAvf,
                                    receivedText
                                )
                            )
                            callbackDeferredTTS4 = null
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
    // Read thread cash box
    private val readThreadCashBox = object : Thread() {
        @SuppressLint("SuspiciousIndentation")
        override fun run() {
            Logger.info("PortConnectionDataSource: start read thread cash box")
            while (!currentThread().isInterrupted) {
                try {
                    portConnectionHelperDataSource.startReadingCashBox(512) { data ->
                        Logger.info("-------> data from cash box: ${byteArrayToHexString(data)}")
                        coroutineScope.launch {
                            _dataFromCashBox.emit(data)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    // Send command vending machine
    fun sendCommandVendingMachine(byteArray: ByteArray) : Int {
        Logger.debug("data vending machine send: ${byteArrayToHexString(byteArray)}")
        return portConnectionHelperDataSource.writeDataPortVendingMachine(byteArray)
    }

    // Send command vending machine
    fun sendCommandUsbVendingMachine(byteArray: ByteArray) : Int {
        Logger.debug("data vending machine send: ${byteArrayToHexString(byteArray)}")
        return portConnectionHelperDataSource.writeDataUsbPortVendingMachine(byteArray)
    }

    // Send command cash box
    fun sendCommandCashBox(byteArray: ByteArray) : Int {
        Logger.debug("data cash box send: ${byteArrayToHexString(byteArray)}")
        return portConnectionHelperDataSource.writeDataPortCashBox(byteArray)
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun startTimerCommunicateTTYS4() {
        timerCommunicateTTYS4 = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                GlobalScope.launch(Dispatchers.Main) {


                    notifyTypeRXCommunicateAvfChanged(TypeRXCommunicateAvf.TIME_OUT)
                    Log.d("RX Avf ttyS4", "TypeRXCommunicateAvf.TIME_OUT")
                    callbackDeferredTTS4?.complete(
                        DataRxCommunicateTTS4(
                            TypeRXCommunicateAvf.TIME_OUT,
                            ""
                        )
                    )
                }
                timerCommunicateTTYS4 = null
                // Perform actions when the timer completes
            }
        }.start()
    }
    private fun notifyTypeRXCommunicateAvfChanged(typeRXCommunicateAvf: TypeRXCommunicateAvf) {
        listeners.forEach { listener ->
            listener.onTypeRXCommunicateAvfChanged(typeRXCommunicateAvf)
        }
    }

    private fun notifyTypeTXCommunicateAvfChanged(typeTXCommunicateAvf: TypeTXCommunicateAvf) {
        listeners.forEach { listener ->
            listener.onTypeTXCommunicateAvfChanged(typeTXCommunicateAvf)

        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun sendCommandVendingMachineAsync(
        byteArray: ByteArray, typeTXCommunicateAvf: TypeTXCommunicateAvf,
        callbackDeferredTTS4: CompletableDeferred<DataRxCommunicateTTS4>
    ) {
//        Log.d("logcatportsettings", "send data vending machine: $byteArray")

        if (fdPortVendingMachine != -1 && timerCommunicateTTYS4 == null) {
            typeRXCommunicateAvf = TypeRXCommunicateAvf.UNKNOWN
            this.typeTXCommunicateAvf = typeTXCommunicateAvf
            Log.d(
                "Communicate TX Avf machine",
                "${this.typeTXCommunicateAvf}"
            )
            this.callbackDeferredTTS4 = callbackDeferredTTS4
            startTimerCommunicateTTYS4()
            sendCommandVendingMachine(byteArray)

            GlobalScope.launch(Dispatchers.Main) {
                notifyTypeTXCommunicateAvfChanged(typeTXCommunicateAvf)
            }

        } else if (timerCommunicateTTYS4 != null) {

            Log.d("logcatportsettings", "$typeTXCommunicateAvf")
        } else {

            Log.d("logcatportsettings", "/dev/ttS4 not open")
        }
    }


    // Array byte to hex string
    private fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(",") { "%02X".format(it) }
    }

    public suspend fun enquirySlot(
        numberBoard: Int = 0,
        slot: Int,
    ): DataRxCommunicateTTS4 {
        val callback = CompletableDeferred<DataRxCommunicateTTS4>()
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

        sendCommandVendingMachineAsync(byteArray, TypeTXCommunicateAvf.ENQUIRY_SLOT, callback)
        return callbackDeferredTTS4?.await() ?: DataRxCommunicateTTS4(TypeRXCommunicateAvf.UNKNOWN)
    }
    private fun cancelTimerCommunicateTTYS4() {
        timerCommunicateTTYS4?.cancel()
        timerCommunicateTTYS4 = null
    }
}
interface TypeRXCommunicateAvfListener {
    fun onTypeRXCommunicateAvfChanged(typeRXCommunicateAvf: TypeRXCommunicateAvf)
    fun onTypeTXCommunicateAvfChanged(typeTXCommunicateAvf: TypeTXCommunicateAvf)
}