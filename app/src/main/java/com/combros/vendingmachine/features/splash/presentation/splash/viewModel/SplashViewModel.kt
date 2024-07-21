package com.combros.vendingmachine.features.splash.presentation.splash.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.reflect.TypeToken
import com.combros.vendingmachine.ScheduledTaskWorker
import com.combros.vendingmachine.common.base.domain.model.InitSetup
import com.combros.vendingmachine.common.base.domain.repository.BaseRepository
import com.combros.vendingmachine.core.util.Logger
import com.combros.vendingmachine.core.util.Screens
import com.combros.vendingmachine.core.util.pathFileInitSetup
import com.combros.vendingmachine.features.splash.presentation.splash.viewState.SplashViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
@Inject constructor(
    private val baseRepository: BaseRepository,
    @ApplicationContext private val context: Context,
    private val logger: Logger,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashViewState())
    val state = _state.asStateFlow()
    private val workManager = WorkManager.getInstance(context)

    fun handleInit (navController: NavHostController) {
        logger.info("handleInit")
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val isFileInitSetupExists = baseRepository.isFileExists(pathFileInitSetup)
                logger.debug("isFileInitSetupExists: $isFileInitSetupExists")
                if (isFileInitSetupExists) {
                    val initSetup: InitSetup? = baseRepository.getDataFromLocal(
                        type = object : TypeToken<InitSetup>() {}.type,
                        path = pathFileInitSetup
                    )
                    if (initSetup != null) {
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
                        scheduleDailyTask("ResetAppTask", hourReset, minuteReset)

                        if(initSetup.currentCash > 0) {
                            baseRepository.addNewDepositWithdrawLogToLocal(
                                machineCode = initSetup.vendCode,
                                transactionType = "withdraw",
                                denominationType = initSetup.currentCash,
                                quantity = 1,
                                status = "unknown",
                                currentBalance = 0,
                            )
                            initSetup.currentCash = 0
                            baseRepository.writeDataToLocal(
                                data = initSetup,
                                path = pathFileInitSetup,
                            )
                        }

                        navController.navigate(Screens.HomeScreenRoute.route) {
                            popUpTo(Screens.SplashScreenRoute.route) {
                                inclusive = true
                            }
                        }
                    } else {
                        navController.navigate(Screens.InitSetupScreenRoute.route) {
                            popUpTo(Screens.SplashScreenRoute.route) {
                                inclusive = true
                            }
                        }
                    }
                } else {
                    navController.navigate(Screens.InitSetupScreenRoute.route) {
                        popUpTo(Screens.SplashScreenRoute.route) {
                            inclusive = true
                        }
                    }
                }
            } catch (e: Exception) {
                baseRepository.addNewErrorLogToLocal(
                    machineCode = "error when machine code has not been entered",
                    errorContent = "handleInit fail in SplashViewModel/handleInit(): ${e.message}",
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
}