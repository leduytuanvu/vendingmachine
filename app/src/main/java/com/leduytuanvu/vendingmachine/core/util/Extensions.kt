package com.leduytuanvu.vendingmachine.core.util

import android.util.Log
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.common.models.LogException
import com.leduytuanvu.vendingmachine.core.errors.CustomError
//import com.leduytuanvu.vendingmachine.core.room.LogException
import com.leduytuanvu.vendingmachine.core.datasource.local_storage_datasource.LocalStorageDatasource
import com.leduytuanvu.vendingmachine.features.settings.data.model.response.SlotResponse
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

fun LocalDateTime.currentDateTimeString(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
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
            String.format("%02d", this) // Add leading zero if number is between 0 and 9
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

fun Exception.exceptionToCustomError(function: String): CustomError {
    try {
        return CustomError.GeneralException(
            message = message,
            function = function,
            localizedMessage = localizedMessage,
            cause = cause.toString(),
        )
    } catch (e: Exception) {
        throw e
    }
}

suspend fun Exception.exceptionHandling(
    localStorageDatasource: LocalStorageDatasource,
    exception: Exception,
    inFunction: String,
    eventType: String = "Other",
    typeException: String = "Software",
    dataJson: String = "",
): Boolean {
    try {
        val logException = LogException(
            eventType = eventType,
            eventTime = LocalDateTime.now().currentDateTimeString(),
            message = exception.message ?: "",
            inFunction = inFunction,
            typeException = typeException,
            eventData = dataJson,
            isSent = false
        )
        var listLogException: ArrayList<LogException> = arrayListOf()
        if(localStorageDatasource.checkFileExists(localStorageDatasource.fileLogException)) {
            val json = localStorageDatasource.readData(localStorageDatasource.fileLogException)
            listLogException = localStorageDatasource.gson.fromJson(
                json,
                object : TypeToken<ArrayList<LogException>>() {}.type
            ) ?: arrayListOf()
        }
        listLogException.add(logException)
        localStorageDatasource.writeData(localStorageDatasource.fileLogException, localStorageDatasource.gson.toJson(listLogException))
        EventBus.sendEvent(Event.Toast(logException.message!!))
        return true
    } catch (e: Exception) {
        Logger.error("Error in exception handling: ${e.toString()}")
    }
    return false
}

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
        isLock = false
    )
}