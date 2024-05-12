package com.leduytuanvu.vendingmachine.core.errors

import com.leduytuanvu.vendingmachine.core.util.toDateTimeString
import org.threeten.bp.LocalDateTime

sealed class CustomError {
    data class NoNetworkConnection(
        val message: String?,
        val dateTime: String? = LocalDateTime.now().toDateTimeString(),
        val function: String? = null,
        val localizedMessage: String? = null,
        val cause: String? = null,
    ) : CustomError()

    data class UnknownError(
        val message: String?,
        val dateTime: String? = LocalDateTime.now().toDateTimeString(),
        val function: String? = null,
        val localizedMessage: String? = null,
        val cause: String? = null,
    ) : CustomError()

    data class GeneralError(
        val message: String?,
        val dateTime: String? = LocalDateTime.now().toDateTimeString(),
        val function: String? = null,
        val localizedMessage: String? = null,
        val cause: String? = null,
    ) : CustomError()

    data class GeneralException(
        val message: String?,
        val dateTime: String? = LocalDateTime.now().toDateTimeString(),
        val function: String? = null,
        val localizedMessage: String? = null,
        val cause: String? = null,
    ) : CustomError()
}