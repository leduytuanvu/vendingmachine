package com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.viewModel

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.Coil
import coil.request.ImageRequest
import com.leduytuanvu.vendingmachine.ScheduledTaskWorker
import com.leduytuanvu.vendingmachine.common.base.domain.model.InitSetup
import com.leduytuanvu.vendingmachine.common.base.domain.repository.BaseRepository
import com.leduytuanvu.vendingmachine.core.datasource.portConnectionDatasource.PortConnectionDatasource
import com.leduytuanvu.vendingmachine.core.util.ByteArrays
import com.leduytuanvu.vendingmachine.core.util.Event
import com.leduytuanvu.vendingmachine.core.util.Logger
import com.leduytuanvu.vendingmachine.core.util.Screens
import com.leduytuanvu.vendingmachine.core.util.pathFileInitSetup
import com.leduytuanvu.vendingmachine.core.util.pathFilePaymentMethod
import com.leduytuanvu.vendingmachine.core.util.pathFileSlot
import com.leduytuanvu.vendingmachine.core.util.pathFolderImagePayment
import com.leduytuanvu.vendingmachine.core.util.pathFolderImageProduct
import com.leduytuanvu.vendingmachine.core.util.sendEvent
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.ActivateTheMachineRequest
import com.leduytuanvu.vendingmachine.features.auth.data.model.request.LoginRequest
import com.leduytuanvu.vendingmachine.features.auth.domain.repository.AuthRepository
import com.leduytuanvu.vendingmachine.features.settings.domain.model.Slot
import com.leduytuanvu.vendingmachine.features.settings.domain.repository.SettingsRepository
import com.leduytuanvu.vendingmachine.features.splash.presentation.initSetup.viewState.InitSetupViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Arrays
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class InitSetupViewModel @Inject constructor(
    private val baseRepository: BaseRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger,
    private val byteArrays: ByteArrays,
    private val portConnectionDatasource: PortConnectionDatasource,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state = MutableStateFlow(InitSetupViewState())
    val state = _state.asStateFlow()
    private val workManager = WorkManager.getInstance(context)

    private fun showDialogWarning(mess: String) {
        viewModelScope.launch {
            _state.update {
                it.copy (
                    titleDialogWarning = mess,
                    isWarning = true,
                )
            }
        }
    }

    fun hideDialogWarning() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isWarning = false,
                    titleDialogWarning = "",
                )
            }
        }
    }

    fun writeInitSetupToLocal(
        inputVendingMachineCode: String,
        portCashBox: String,
        portVendingMachine: String,
        typeVendingMachine: String,
        loginRequest: LoginRequest,
        navController: NavHostController,
    ) {
        logger.info("writeInitSetupToLocal")
        viewModelScope.launch {
            try {

                if (baseRepository.isHaveNetwork(context)) {
                    _state.update { it.copy(isLoading = true) }
                    if (inputVendingMachineCode.trim().isEmpty()) {
                        sendEvent(Event.Toast("Vending machine code must not empty!"))
                    } else if (portCashBox.isEmpty()) {
                        sendEvent(Event.Toast("Port cash box must not empty!"))
                    } else if (portVendingMachine.isEmpty()) {
                        sendEvent(Event.Toast("Port vending machine must not empty!"))
                    } else if (typeVendingMachine.isEmpty()) {
                        sendEvent(Event.Toast("Type vending machine must not empty!"))
                    } else if (loginRequest.username.isEmpty()) {
                        sendEvent(Event.Toast("Username must not empty!"))
                    } else if (loginRequest.password.isEmpty()) {
                        sendEvent(Event.Toast("Password must not empty!"))
                    } else {
                        val passwordEncode = authRepository.encodePassword(loginRequest.password)
                        logger.debug("1")
                        val responseLogin = authRepository.login(inputVendingMachineCode, loginRequest)
                        logger.debug("2")
                        if (responseLogin.accessToken!!.isNotEmpty()) {
                            logger.debug("3")
                            baseRepository.addNewAuthyLogToLocal(
                                machineCode = inputVendingMachineCode,
                                authyType = "login",
                                username = loginRequest.username,
                            )
                            logger.debug("3.0")
                            val baudRateCashBox = "9600"
                            logger.debug("3.1")
                            val baudRateVendingMachine = "9600"
                            logger.debug("3.2")
                            val androidId = baseRepository.getAndroidId()
                            logger.debug("3.3")
                            val initSetup = InitSetup(
                                vendCode = inputVendingMachineCode,
                                androidId = androidId,
                                username = loginRequest.username,
                                password = passwordEncode,
                                portVendingMachine = portVendingMachine,
                                baudRateVendingMachine = baudRateVendingMachine,
                                portCashBox = portCashBox,
                                baudRateCashBox = baudRateCashBox,
                                typeVendingMachine = typeVendingMachine,
                                fullScreenAds = "ON",
                                withdrawalAllowed = "ON",
                                autoStartApplication = "ON",
                                layoutHomeScreen = "3",
                                timeTurnOnLight = "18:00",
                                timeTurnOffLight = "06:00",
                                dropSensor = "ON",
                                inchingMode = "0",
                                timeoutJumpToBigAdsScreen = "60",
                                glassHeatingMode = "ON",
                                highestTempWarning = "25",
                                lowestTempWarning = "0",
                                temperature = "25",
                                initPromotion = "OFF",
                                currentCash = 0,
                                timeoutPaymentByCash = "60",
                                timeoutPaymentByQrCode = "60",
                                timeResetOnEveryDay = "00:00",
                                role = ""
                            )
                            logger.debug("4")
                            baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                            val responseGetListAccount = authRepository.getListAccount(inputVendingMachineCode)
                            logger.debug("5")

                            if(responseGetListAccount.code == 200) {
                                logger.debug("6")
                                val index = responseGetListAccount.data.indexOfFirst { it.username == loginRequest.username }
//                                if(index != -1 && !responseGetListAccount.data[index].role.isNullOrEmpty()) {
                                if(index != -1) {
                                    logger.debug("7")
//                                    initSetup.role = responseGetListAccount.data[index].role!!
                                    initSetup.role = "admin"
                                    baseRepository.writeDataToLocal(data = initSetup, path = pathFileInitSetup)
                                    val activateTheMachineRequest = ActivateTheMachineRequest(
                                        machineCode = inputVendingMachineCode,
                                        androidId = androidId,
                                    )
                                    val responseActivateTheMachine = authRepository.activateTheMachine(activateTheMachineRequest)
                                    logger.debug("8")
                                    if(responseActivateTheMachine.code==200 || responseActivateTheMachine.code == 400) {
                                        logger.debug("9")
                                        val listPaymentMethod = settingsRepository.getListPaymentMethodFromServer()
                                        if (!baseRepository.isFolderExists(pathFolderImagePayment)) {
                                            baseRepository.createFolder(pathFolderImagePayment)
                                        }
                                        for (item in listPaymentMethod) {
                                            if(item.imageUrl!!.isNotEmpty()) {
                                                var notHaveError = true
                                                for (i in 1..3) {
                                                    try {
                                                        val request = ImageRequest.Builder(context = context)
                                                            .data(item.imageUrl)
                                                            .build()
                                                        val result = withContext(Dispatchers.IO) {
                                                            Coil.imageLoader(context).execute(request).drawable
                                                        }
                                                        if (result != null) {
                                                            val file =
                                                                File(pathFolderImagePayment, "${item.methodName}.png")
                                                            withContext(Dispatchers.IO) {
                                                                file.outputStream().use { outputStream ->
                                                                    result.toBitmap().compress(
                                                                        Bitmap.CompressFormat.PNG,
                                                                        1,
                                                                        outputStream
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        logger.debug("download ${item.imageUrl} success")
                                                    } catch (e: Exception) {
                                                        notHaveError = false
                                                        logger.debug("${e.message}")
                                                    } finally {
                                                        if (notHaveError) break
                                                    }
                                                }
                                            }
                                        }
                                        logger.debug("10")
                                        baseRepository.writeDataToLocal(data = listPaymentMethod, path = pathFilePaymentMethod)
                                        val listSlot = arrayListOf<Slot>()
                                        for(i in 1..60) {
                                            listSlot.add(
                                                Slot(
                                                    slot = i,
                                                    productCode = "",
                                                    productName = "",
                                                    inventory = 10,
                                                    capacity = 10,
                                                    price = 10000,
                                                    isCombine = "no",
                                                    springType = "lo xo don",
                                                    status = 1,
                                                    slotCombine = 0,
                                                    isLock = false,
                                                    isEnable = true
                                                )
                                            )
                                        }
                                        logger.debug("11")
                                        baseRepository.writeDataToLocal(listSlot, pathFileSlot)
                                        val partsTimeTurnOnLight = initSetup.timeTurnOnLight.split(":")
                                        val hourTurnOnLight = partsTimeTurnOnLight[0].toInt()
                                        val minuteTurnOnLight = partsTimeTurnOnLight[1].toInt()
                                        rescheduleDailyTask("TurnOnLightTask", hourTurnOnLight, minuteTurnOnLight)
                                        val partsTimeTurnOffLight = initSetup.timeTurnOffLight.split(":")
                                        val hourTurnOffLight = partsTimeTurnOffLight[0].toInt()
                                        val minuteTurnOffLight = partsTimeTurnOffLight[1].toInt()
                                        rescheduleDailyTask("TurnOffLightTask", hourTurnOffLight, minuteTurnOffLight)
                                        val partsTimeResetApp = initSetup.timeResetOnEveryDay.split(":")
                                        val hourReset = partsTimeResetApp[0].toInt()
                                        val minuteReset = partsTimeResetApp[1].toInt()
                                        rescheduleDailyTask("ResetAppTask", hourReset, minuteReset)
                                        baseRepository.addNewSetupLogToLocal(
                                            machineCode = inputVendingMachineCode,
                                            operationContent = "setup init: $initSetup",
                                            operationType = "setup system",
                                            username = loginRequest.username,
                                        )
                                        logger.debug("12")
                                        navController.navigate(Screens.SettingScreenRoute.route) {
                                            popUpTo(Screens.InitSetupScreenRoute.route) {
                                                inclusive = true
                                            }
                                        }
                                        sendEvent(Event.Toast("Setup init success"))
                                    } else {
                                        baseRepository.deleteFile(pathFileInitSetup)
                                        baseRepository.addNewErrorLogToLocal(
                                            machineCode = initSetup.vendCode,
                                            errorContent = "call api activate the machine fail in InitSetupViewModel/writeInitSetupToLocal(): ${responseActivateTheMachine.message}",
                                        )
                                    }
                                } else {
                                    baseRepository.deleteFile(pathFileInitSetup)
                                    baseRepository.addNewErrorLogToLocal(
                                        machineCode = initSetup.vendCode,
                                        errorContent = "not found account or role is null/empty in get list account from server in InitSetupViewModel/writeInitSetupToLocal()",
                                    )
                                }
                            } else {
                                baseRepository.deleteFile(pathFileInitSetup)
                                baseRepository.addNewErrorLogToLocal(
                                    machineCode = initSetup.vendCode,
                                    errorContent = "get list account from server fail in InitSetupViewModel/writeInitSetupToLocal()",
                                )
                            }
                        } else {
                            baseRepository.addNewErrorLogToLocal(
                                machineCode = "error when machine code has not been entered",
                                errorContent = "access token get from api is empty in InitSetupViewModel/writeInitSetupToLocal()",
                            )
                        }
                    }
                } else {
                    showDialogWarning("Not have internet, please connect with internet!")
                }
            } catch (e: Exception) {
                if(baseRepository.isFileExists(pathFileInitSetup)) {
                    baseRepository.deleteFile(pathFileInitSetup)
                }
                baseRepository.addNewErrorLogToLocal(
                    machineCode = "error when machine code has not been entered",
                    errorContent = "write init setup to local in the first time fail in InitSetupViewModel/writeInitSetupToLocal(): ${e.message}",
                )
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun rescheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        workManager.cancelUniqueWork(taskName).also {
            scheduleDailyTask(taskName, hour, minute)
        }
    }

    private fun scheduleDailyTask(taskName: String, hour: Int, minute: Int) {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(currentTime)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val initialDelay = targetTime.timeInMillis - currentTime.timeInMillis
        val inputData = Data.Builder()
            .putString("TASK_NAME", taskName)
            .build()
        val dailyWorkRequest = PeriodicWorkRequestBuilder<ScheduledTaskWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .build()
        workManager.enqueueUniquePeriodicWork(
            taskName, // Unique task name
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
    private var check = false
    fun getListSerialPort()  {

        logger.debug("=== list ${portConnectionDatasource.getListSerialPort().contentToString()}")
    }
    fun checkCommand(){
        if (check){
            portConnectionDatasource.sendCommandVendingMachine(byteArray = byteArrays.vmTurnOffLight)
        }else{
            portConnectionDatasource.sendCommandVendingMachine(byteArray = byteArrays.vmTurnOnLight)
        }
        check = !check
    }

}