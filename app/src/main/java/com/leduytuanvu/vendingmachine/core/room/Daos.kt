package com.leduytuanvu.vendingmachine.core.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LogExceptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: LogException)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: LogException)

    @Delete
    suspend fun delete(item: LogException)

    @Query("SELECT * FROM log_exception")
    fun getAllLogException(): Flow<List<LogException>>

    @Query("SELECT * FROM log_exception WHERE id =:id")
    fun getLogException(id: Int): Flow<LogException>
}