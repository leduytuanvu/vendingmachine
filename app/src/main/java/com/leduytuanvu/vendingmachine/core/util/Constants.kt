package com.leduytuanvu.vendingmachine.core.util

import android.annotation.SuppressLint
import androidx.compose.ui.text.AnnotatedString

//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository


const val BASE_URL = "https://dev-api.avf.vn"

@SuppressLint("SdCardPath")
const val pathFileInitSetup = "/sdcard/VendingMachineData/Setup/InitSetup.json"
@SuppressLint("SdCardPath")
const val pathFileSlot = "/sdcard/VendingMachineData/Slot/Slot.json"
@SuppressLint("SdCardPath")
const val pathFileProductDetail = "/sdcard/VendingMachineData/Product/ProductDetail.json"
@SuppressLint("SdCardPath")
const val pathFolderImage = "/sdcard/VendingMachineData/Product/Image"
@SuppressLint("SdCardPath")
const val pathFileLogException = "/sdcard/VendingMachineData/Log/LogException.json"
@SuppressLint("SdCardPath")
const val pathFileLogServer = "/sdcard/VendingMachineData/Log/LogServer.json"

val itemsPort = listOf(
    AnnotatedString("ttyS1"),
    AnnotatedString("ttyS2"),
    AnnotatedString("ttyS3"),
    AnnotatedString("ttyS4")
)
val itemsTypeVendingMachine = listOf(
    AnnotatedString("XY"),
    AnnotatedString("TCN"),
    AnnotatedString("TCN INTEGRATED CIRCUITS"),
)


