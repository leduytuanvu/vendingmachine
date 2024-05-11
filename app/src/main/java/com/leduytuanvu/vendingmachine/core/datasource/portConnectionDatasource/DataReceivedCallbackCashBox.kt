package com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource

interface DataReceivedCallbackCashBox {
    fun onDataReceivedCashBox(data: ByteArray)
}