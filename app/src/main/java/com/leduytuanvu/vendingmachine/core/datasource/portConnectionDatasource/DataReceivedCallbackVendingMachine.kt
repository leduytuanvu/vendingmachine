package com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource

interface DataReceivedCallbackVendingMachine {
    fun onDataReceivedVendingMachine(data: ByteArray)
}