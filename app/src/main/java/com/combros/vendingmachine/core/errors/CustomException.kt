package com.combros.vendingmachine.core.errors

//import com.leduytuanvu.vendingmachine.core.room.LogException

sealed class CustomException (
    message: String? = null
) : Exception(message) {
    data object NoNetworkConnectionException : CustomException() {
        private fun readResolve(): Any = NoNetworkConnectionException
    }

    data object UnknownException : CustomException() {
        private fun readResolve(): Any = UnknownException
    }

    fun mapToCustomError(function: String) : CustomError {
        return when(this) {
            is NoNetworkConnectionException -> {
                CustomError.NoNetworkConnection(this.message, function)
            }
            is UnknownException -> {
                CustomError.UnknownError(this.message, function)
            }
        }
    }
}