package com.combros.vendingmachine.core.util

import android.annotation.SuppressLint
import androidx.compose.ui.text.AnnotatedString
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale
const val BASE_URL = "https://api.avf.vn"

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
const val pathFileLogUpdateInventoryServer = "/sdcard/VendingMachineData/Log/UpdateInventory.json"
@SuppressLint("SdCardPath")
const val pathFolderAds = "/sdcard/VendingMachineData/Ads/HomeAds"
@SuppressLint("SdCardPath")
const val pathFolderBigAds = "/sdcard/VendingMachineData/Ads/BigAds"
@SuppressLint("SdCardPath")
const val pathFilePaymentMethod = "/sdcard/VendingMachineData/Payment/PaymentMethod.json"
@SuppressLint("SdCardPath")
const val pathFolderImagePayment = "/sdcard/VendingMachineData/Payment/Image"
@SuppressLint("SdCardPath")
const val pathFilePriceOfProduct = "/sdcard/VendingMachineData/Product/PriceOfProduct.json"
@SuppressLint("SdCardPath")
const val pathFileSyncOrder = "/sdcard/VendingMachineData/Log/SyncOrder.json"
@SuppressLint("SdCardPath")
const val pathFileSyncOrderTransaction = "/sdcard/VendingMachineData/Log/SyncOrderTransaction.json"
@SuppressLint("SdCardPath")
const val pathFileUpdatePromotion = "/sdcard/VendingMachineData/Log/UpdatePromotion.json"
@SuppressLint("SdCardPath")
const val pathFileUpdateDeliveryStatus = "/sdcard/VendingMachineData/Log/UpdateDeliveryStatus.json"
@SuppressLint("SdCardPath")
fun pathFileUpdateTrackingAds():String{
    val date = LocalDateTime.now().toYYYYMMdd()
    return "/sdcard/VendingMachineData/Log/TrackingAds/${date}/UpdateTrackingAds.json"
}

val itemsPort = listOf(
    AnnotatedString("ttyS0"),
    AnnotatedString("ttyS1"),
    AnnotatedString("ttyS2"),
    AnnotatedString("ttyS3"),
    AnnotatedString("ttyS4"),
    AnnotatedString("ttyS5"),
    AnnotatedString("ttyS6"),
    AnnotatedString("ttyS7"),
    AnnotatedString("ttyS8"),
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


