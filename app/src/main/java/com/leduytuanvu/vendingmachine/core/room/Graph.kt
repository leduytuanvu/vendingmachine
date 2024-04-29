package com.leduytuanvu.vendingmachine.core.room

import android.content.Context

object Graph {
    lateinit var database:VendingMachineDatabase private set

    val repository by lazy {
        Repository(
            logExceptionDao = database.logExceptionDao(),
        )
    }

    fun provide(context:Context){
        database = VendingMachineDatabase.getDatabase(context)
    }
}