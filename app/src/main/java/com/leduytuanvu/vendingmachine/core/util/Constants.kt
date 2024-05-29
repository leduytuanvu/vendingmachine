package com.leduytuanvu.vendingmachine.core.util

import android.annotation.SuppressLint
import androidx.compose.ui.text.AnnotatedString
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val BASE_URL = "https://dev-api.avf.vn"

@SuppressLint("SdCardPath")
const val pathFolderVendingMachineData = "/sdcard/VendingMachineData"
@SuppressLint("SdCardPath")
const val pathFileInitSetup = "/sdcard/VendingMachineData/Setup/InitSetup.json"
@SuppressLint("SdCardPath")
const val pathFileSlot = "/sdcard/VendingMachineData/Slot/Slot.json"
@SuppressLint("SdCardPath")
const val pathFileProductDetail = "/sdcard/VendingMachineData/Product/ProductDetail.json"
@SuppressLint("SdCardPath")
const val pathFolderImageProduct = "/sdcard/VendingMachineData/Product/Image"
@SuppressLint("SdCardPath")
const val pathFileLogServer = "/sdcard/VendingMachineData/Log/LogServer.json"
@SuppressLint("SdCardPath")
const val pathFileLogDepositWithdrawServer = "/sdcard/VendingMachineData/Log/DepositWithdrawServer.json"
@SuppressLint("SdCardPath")
const val pathFolderAds = "/sdcard/VendingMachineData/Ads"
@SuppressLint("SdCardPath")
const val pathFilePaymentMethod = "/sdcard/VendingMachineData/Payment/PaymentMethod.json"
@SuppressLint("SdCardPath")
const val pathFolderImagePayment = "/sdcard/VendingMachineData/Payment/Image"
@SuppressLint("SdCardPath")
const val pathFilePriceOfProduct = "/sdcard/VendingMachineData/Product/PriceOfProduct.json"

val itemsPort = listOf(
    AnnotatedString("ttyS1"),
    AnnotatedString("ttyS2"),
    AnnotatedString("ttyS3"),
    AnnotatedString("ttyS4"),
    AnnotatedString("ttyS5"),
)

val itemsTypeVendingMachine = listOf(
    AnnotatedString("XY"),
    AnnotatedString("TCN"),
    AnnotatedString("TCN INTEGRATED CIRCUITS"),
)

fun getCurrentDateTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm - dd 'Tháng' MM, yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}


