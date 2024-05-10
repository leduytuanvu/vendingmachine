package com.leduytuanvu.vendingmachine.core.room

class RoomRepository (
    private val logExceptionDao: LogExceptionDao,
) {
    fun getAllLogException() = logExceptionDao.getAllLogException()

    fun getLogException(id: Int) = logExceptionDao.getLogException(id)

    suspend fun insertLogException(logException: LogException) {
        logExceptionDao.insert(logException)
    }

    suspend fun deleteLogException(logException: LogException) {
        logExceptionDao.delete(logException)
    }

    suspend fun updateLogException(logException: LogException) {
        logExceptionDao.update(logException)
    }
}