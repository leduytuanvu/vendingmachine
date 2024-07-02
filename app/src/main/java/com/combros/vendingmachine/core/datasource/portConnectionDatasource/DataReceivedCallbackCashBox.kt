package com.combros.vendingmachine.core.datasource.portConnectionDatasource

interface DataReceivedCallbackCashBox {
    fun onDataReceivedCashBox(data: ByteArray)
}