package com.leduytuanvu.vendingmachine.core.errors

import com.leduytuanvu.vendingmachine.core.util.currentDateTimeString
import org.threeten.bp.LocalDateTime

sealed class CustomError {
    data class NoNetworkConnection(val message: String?, val dateTime: String? = LocalDateTime.now().currentDateTimeString(), val function: String? = null) : CustomError()
    data class UnknownError(val message: String?, val dateTime: String? = LocalDateTime.now().currentDateTimeString(), val function: String? = null) : CustomError()
    data class GeneralError(val message: String?, val dateTime: String? = LocalDateTime.now().currentDateTimeString(), val function: String? = null) : CustomError()
}