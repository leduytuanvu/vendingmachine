package com.combros.vendingmachine.core.datasource.portConnectionDatasource

interface DataReceivedCallbackVendingMachine {
    fun onDataReceivedVendingMachine(data: ByteArray)
}