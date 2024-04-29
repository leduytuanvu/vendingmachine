package com.leduytuanvu.vendingmachine.core.room

class Repository(
    private val logExceptionDao: LogExceptionDao,
) {
    fun getAllLogException() = logExceptionDao.getAllLogException()

    fun getLogException(id: Int) = logExceptionDao.getLogException(id)

    suspend fun insertItem(item: LogException) {
        logExceptionDao.insert(item)
    }

    suspend fun deleteItem(item: LogException) {
        logExceptionDao.delete(item)
    }

    suspend fun updateItem(item: LogException) {
        logExceptionDao.update(item)
    }
}