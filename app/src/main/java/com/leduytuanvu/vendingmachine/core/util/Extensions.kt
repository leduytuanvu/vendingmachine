package com.leduytuanvu.vendingmachine.core.util

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.model.LogError
//import com.leduytuanvu.vendingmachine.core.room.LogException
import com.leduytuanvu.vendingmachine.core.datasource.localStorageDatasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.SlotResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import kotlin.random.Random

fun Random.next14DigitNumber(): Long {
    return (10000000000000L..99999999999999L).random(this)
}

fun LocalDateTime.toDateTimeString(pattern: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): String {
    try {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return this.format(formatter)
    } catch (e: Exception) {
        throw e
    }
}

fun String.toDateTime(format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): LocalDateTime {
    try {
        val formatter = DateTimeFormatter.ofPattern(format)
        return LocalDateTime.parse(this, formatter)
    } catch (e: Exception) {
        throw e
    }
}

fun LocalDateTime.toId(pattern: String = "${Random.next14DigitNumber()}yyyyMMddHHmmssSSS"): String {
    try {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return this.format(formatter)
    } catch (e: Exception) {
        throw e
    }
}

fun Int.toVietNamDong(): String {
    return try {
        String.format("%,d vnđ", this)
    } catch (e: Exception) {
        throw e
    }
}

fun Int.toChooseNumber(): String {
    return try {
        val formattedNumber = if (this in 0..9) {
            String.format("%02d", this)
        } else {
            this.toString()
        }
        formattedNumber
    } catch (e: Exception) {
        throw e
    }
}

fun Int.toChooseNumberMoney(): String {
    return try {
        String.format("%,d", this * 1000)
    } catch (e: Exception) {
        throw e
    }
}

//suspend fun Exception.toLogError (
//    localStorageDatasource: LocalStorageDatasource,
//    errorType: String = "application",
//    eventTime: String = LocalDateTime.now().toDateTimeString(),
//) : LogError {
//    val initSetup: InitSetup = localStorageDatasource.getDataFromPath(pathFileInitSetup)!!
//    return LogError(
//        machine_code = initSetup.vendCode,
//        error_type = errorType,
//        error_content = message ?: "unknown error",
//        event_time = eventTime,
//    )
//}

fun SlotResponse?.toSlot(): Slot {
    return Slot(
        slot = this?.slot ?: 0,
        productCode = this?.productCode ?: "",
        productName = "",
        inventory = 10,
        capacity = 10,
        price = 0,
        isCombine = this?.isCombine ?: "",
        springType = this?.springType ?: "",
        status = this?.status ?: 0,
        slotCombine = this?.slotCombine ?: 0,
        isLock = false,
        isEnable = true
    )
}

fun <T> T?.toBase64(): String {
    if (this == null) return ""
    val gson = Gson()
    val json = gson.toJson(this)
    return Base64.encodeToString(json.toByteArray(), Base64.DEFAULT)
}

fun String?.toJson(): String {
    if (isNullOrEmpty()) return ""
    val jsonBytes = Base64.decode(this, Base64.DEFAULT)
    return String(jsonBytes)
}

fun Bitmap.toImageBitmap(): ImageBitmap = this.asImageBitmap()

