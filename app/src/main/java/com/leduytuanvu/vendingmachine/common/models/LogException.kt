package com.leduytuanvu.vendingmachine.common.models

//import androidx.room.PrimaryKey

data class LogException(
    val id: Int = 0,
    var eventType: String?,
    var eventTime: String?,
    var eventData: String?,
    var message: String?,
    var typeException: String?,
    var inFunction: String?,
    var isSent: Boolean,
)