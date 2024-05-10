package com.leduytuanvu.vendingmachine.core.room

import android.content.Context

object Graph {
    private lateinit var database:VendingMachineDatabase

    val repository by lazy {
        RoomRepository(
            logExceptionDao = database.logExceptionDao(),
        )
    }

    fun provide(context:Context){
        database = VendingMachineDatabase.getDatabase(context)
    }
}