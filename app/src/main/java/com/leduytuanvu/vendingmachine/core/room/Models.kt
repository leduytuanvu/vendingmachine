package com.leduytuanvu.vendingmachine.core.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_exception")
data class LogException(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String?,
    val dateTime: String?,
    val function: String?,
    val localizedMessage: String?,
    val cause: String?,
)