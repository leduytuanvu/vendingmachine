package com.leduytuanvu.vendingmachine.core.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LogException(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String?,
    val dateTime: String?,
    val function: String?,
)