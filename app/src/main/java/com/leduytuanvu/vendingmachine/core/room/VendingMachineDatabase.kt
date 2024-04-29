package com.leduytuanvu.vendingmachine.core.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LogException::class],
    version = 1,
    exportSchema = false
)
abstract class VendingMachineDatabase:RoomDatabase() {
    abstract fun logExceptionDao():LogExceptionDao

    companion object{
        @Volatile
        var INSTANCE:VendingMachineDatabase? = null
        fun getDatabase(context:Context):VendingMachineDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    VendingMachineDatabase::class.java,
                    "vending_machine_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }

    }

}