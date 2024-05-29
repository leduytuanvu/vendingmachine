package com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource

import android.annotation.SuppressLint
import com.leduytuanvu.vendingmachine.core.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

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

    // Open port vending machine
    fun openPortVendingMachine(port: String) : Int {
        fdPortVendingMachine = portConnectionHelperDataSource.openPortVendingMachine("/dev/", port, 9600)
        return fdPortVendingMachine
    }

    // Open port cash box
    fun openPortCashBox(port: String) : Int {
        fdPortCashBox = portConnectionHelperDataSource.openPortCashBox("/dev/", port, 9600)
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
    private val readThreadVendingMachine = object : Thread() {
        @SuppressLint("SuspiciousIndentation")
        override fun run() {
            Logger.info("PortConnectionDataSource: start read thread vending machine")
            while (!currentThread().isInterrupted) {
                try {
                    portConnectionHelperDataSource.startReadingVendingMachine(512) { data ->
//                        Logger.info("-------> data from vending machine: ${byteArrayToHexString(data)}")
                        coroutineScope.launch {
                            _dataFromVendingMachine.emit(data)
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
//                        Logger.info("-------> data from cash box: ${byteArrayToHexString(data)}")
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
        Logger.info("data vending machine send: ${byteArrayToHexString(byteArray)}")
        return portConnectionHelperDataSource.writeDataPortVendingMachine(byteArray)
    }
    // Send command cash box
    fun sendCommandCashBox(byteArray: ByteArray) : Int {
//        Logger.debug("data cash box send: ${byteArrayToHexString(byteArray)}")
        return portConnectionHelperDataSource.writeDataPortCashBox(byteArray)
    }

    // Array byte to hex string
    private fun byteArrayToHexString(byteArray: ByteArray): String {
        return byteArray.joinToString(",") { "%02X".format(it) }
    }
}