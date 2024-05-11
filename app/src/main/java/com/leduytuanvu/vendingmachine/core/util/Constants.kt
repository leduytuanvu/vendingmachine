package com.leduytuanvu.vendingmachine.core.util

import android.util.Base64
import android.util.Log
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.leduytuanvu.vendingmachine.R
import com.leduytuanvu.vendingmachine.common.models.InitSetup
import com.leduytuanvu.vendingmachine.common.models.LogException
//import com.leduytuanvu.vendingmachine.core.room.Graph
//import com.leduytuanvu.vendingmachine.core.room.LogException
//import com.leduytuanvu.vendingmachine.core.room.RoomRepository
import com.leduytuanvu.vendingmachine.core.storage.LocalStorage
import com.leduytuanvu.vendingmachine.core.util.EventBus.sendEvent
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDateTime
import java.lang.reflect.Type

object Constants {
    const val BASE_URL = "https://dev-api.avf.vn"

//    suspend fun exceptionHandling(
//        localStorage: LocalStorage,
//        exception: Exception,
//        inFunction: String,
//        eventType: String = "Other",
//        typeException: String = "Software",
//        dataJson: String = "",
//    ) {
//        try {
//            val logException = LogException(
//                eventType = eventType,
//                eventTime = LocalDateTime.now().currentDateTimeString(),
//                message = exception.message ?: "",
//                inFunction = inFunction,
//                typeException = typeException,
//                eventData = dataJson,
//                isSent = false
//            )
//            val json = localStorage.readData(localStorage.fileLogException)
//            val listLogException: ArrayList<LogException> = localStorage.gson.fromJson(
//                json,
//                object : TypeToken<ArrayList<LogException>>() {}.type
//            ) ?: arrayListOf()
//            listLogException.add(logException)
//            localStorage.writeData(localStorage.fileLogException, localStorage.gson.toJson(listLogException))
//            sendEvent(Event.Toast(logException.message!!))
//        } catch (e: Exception) {
//            Log.d("tuanvulog", "Error in exception handling: ${e.toString()}")
//        }
//    }
}