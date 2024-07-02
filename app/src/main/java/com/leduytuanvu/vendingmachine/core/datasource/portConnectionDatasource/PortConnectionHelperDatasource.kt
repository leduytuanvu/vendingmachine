package com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource

import com.leduytuanvu.vendingmachine.core.util.Logger

class PortConnectionHelperDatasource {
    init {
        // Load the JNI library
        System.loadLibrary("NativePortCommunication")
        Logger.info("PortConnectionHelperDataSource: loading native port communication library successfully")
    }

    // Open, close, read, write vending machine port
    external fun openPortVendingMachine(path: String, portName: String, baudRate: Int): Int
    external fun openUsbPortVendingMachine(path: String, portName: String, baudRate: Int): Int

    external fun openPortVendingMachineXY(path: String, portName: String, baudRate: Int): Int

    external fun closePortVendingMachine()
    external fun closeUsbPortVendingMachine()

    private external fun readDataPortVendingMachine(bufferSize: Int, callback: DataReceivedCallbackVendingMachine)
    external fun writeDataPortVendingMachine(data: ByteArray): Int

    private external fun readDataUsbPortVendingMachine(bufferSize: Int, callback: DataReceivedCallbackVendingMachine)
    external fun writeDataUsbPortVendingMachine(data: ByteArray): Int

    // Open, close, read, write vending machine port
    external fun openPortCashBox(path: String, portName: String, baudRate: Int): Int
    external fun closePortCashBox()
    private external fun readDataPortCashBox(bufferSize: Int, callback: DataReceivedCallbackCashBox)
    external fun writeDataPortCashBox(data: ByteArray): Int

    // Get all serial ports
    external fun getAllSerialPorts(): Array<String>
    external fun getAllSerialPortsStatus(): Array<Pair<String, Boolean>>

    // Start reading vending machine port
    fun startReadingUsbVendingMachine(bufferSize: Int, callback: (ByteArray) -> Unit) {
        val readDataCallbackVendingMachine = object : DataReceivedCallbackVendingMachine {
            override fun onDataReceivedVendingMachine(data: ByteArray) {
                if (data.size >= 5) callback(data)
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    Logger.error("PortConnectionHelperDataSource: ${e.message}")
                }
            }
        }
        readDataPortVendingMachine(bufferSize, readDataCallbackVendingMachine)
    }

    // Start reading vending machine port
    fun startReadingVendingMachine(bufferSize: Int, callback: (ByteArray) -> Unit) {
        val readDataCallbackVendingMachine = object : DataReceivedCallbackVendingMachine {
            override fun onDataReceivedVendingMachine(data: ByteArray) {
                if (data.size >= 5) callback(data)
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    Logger.error("PortConnectionHelperDataSource: ${e.message}")
                }
            }
        }
        readDataPortVendingMachine(bufferSize, readDataCallbackVendingMachine)
    }

    // Start reading cash box port
    fun startReadingCashBox(bufferSize: Int, callback: (ByteArray) -> Unit) {
        val readDataCallbackCashBox = object : DataReceivedCallbackCashBox {
            override fun onDataReceivedCashBox(data: ByteArray) {
                if (data.size >= 5) callback(data)
                try {
                    Thread.sleep(50)
                } catch (e: InterruptedException) {
                    Logger.error("PortConnectionHelperDataSource: ${e.message}")
                }
            }
        }
        readDataPortCashBox(bufferSize, readDataCallbackCashBox)
    }
}