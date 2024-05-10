package com.leduytuanvu.vendingmachine.core.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_exception")
data class LogException(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var eventType: String?,
    var eventTime: String?,
    var eventData: String?,
    var message: String?,
    var typeException: String?,
    var inFunction: String?,
    var isSent: Boolean,
)